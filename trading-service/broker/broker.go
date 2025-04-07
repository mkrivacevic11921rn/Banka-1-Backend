package broker

import (
	"context"
	"encoding/json"
	"log"
	"time"

	"github.com/Azure/go-amqp"
)

var conn *amqp.Conn
var session *amqp.Session

func Connect(ctx context.Context) {
	c, err := amqp.Dial(ctx, "amqp://127.0.0.1", &amqp.ConnOptions{
		SASLType: amqp.SASLTypeAnonymous(),
	})
	if err != nil {
		log.Fatalf("Konekcija sa brokerom nije uspela: %v", err)
	}
	conn = c

	s, err := conn.NewSession(ctx, nil)
	if err != nil {
		log.Fatalf("Pravljenje broker sesije nije uspelo: %v", err)
	}
	session = s
}

func messageValueAsBytes(message *amqp.Message) []byte {
	var bytes []byte
	if uints, success := message.Value.([]uint8); success {
		bytes = []byte(uints)
	} else if str, success := message.Value.(string); success {
		bytes = []byte(str)
	} else {
		log.Printf("Neuspelo pretvaranje u []byte poruke: %v", message)
		bytes = []byte{}
	}
	return bytes
}

func sendAndRecieve(address string, object any, response any) error {
	ctx, cancel := context.WithTimeout(context.TODO(), time.Minute)
	defer cancel()
	reciever, err := session.NewReceiver(ctx, "4.45", &amqp.ReceiverOptions{
		DynamicAddress: true,
		ExpiryPolicy:   amqp.ExpiryPolicyLinkDetach,
		ExpiryTimeout:  1,
	})
	if err != nil {
		log.Printf("Neuspelo kreiranje receiver-a: %v", err)
		return err
	}
	errorChan := make(chan error)

	go func() {
		defer reciever.Close(ctx)
		var err error
		responseMessage, err := reciever.Receive(ctx, nil)
		if err != nil {
			log.Printf("Neuspesan recieve: %v", err)
			errorChan <- err
			return
		}

		err = reciever.AcceptMessage(ctx, responseMessage)
		if err != nil {
			log.Printf("Neuspesno prihvatanje reply-a: %v", err)
			errorChan <- err
			return
		}

		err = json.Unmarshal(messageValueAsBytes(responseMessage), response)
		if err != nil {
			log.Printf("Neuspesno parsiranje reply-a: %v", err)
			errorChan <- err
			return
		}

		errorChan <- nil
	}()

	body, err := json.Marshal(object)
	if err != nil {
		log.Printf("Neuspelo pretvaranje poruke u JSON: %v", err)
		return err
	}

	addr := reciever.Address()
	message := &amqp.Message{
		Value: body,
		Properties: &amqp.MessageProperties{
			ReplyTo: &addr,
		},
	}

	sender, err := session.NewSender(ctx, address, nil)
	if err != nil {
		log.Printf("Neuspelo kreiranje sender-a: %v", err)
		return err
	}
	defer sender.Close(ctx)

	err = sender.Send(ctx, message, nil)

	if err != nil {
		log.Printf("Neuspesan send: %v", err)
		return err
	}

	select {
	case err := <-errorChan:
		return err
	case <-ctx.Done():
		return ctx.Err()
	}
}

func send(address string, object any) error {
	ctx, cancel := context.WithTimeout(context.TODO(), time.Minute)
	defer cancel()

	body, err := json.Marshal(object)
	if err != nil {
		log.Printf("Neuspelo pretvaranje poruke u JSON: %v", err)
		return err
	}

	message := &amqp.Message{
		Value: body,
	}

	sender, err := session.NewSender(ctx, address, nil)
	if err != nil {
		log.Printf("Neuspelo kreiranje sender-a: %v", err)
		return err
	}
	defer sender.Close(ctx)

	err = sender.Send(ctx, message, nil)

	if err != nil {
		log.Printf("Neuspesan send: %v", err)
		return err
	}

	return nil
}

func sendReliable(address string, object any) error {
	ctx := context.TODO()

	body, err := json.Marshal(object)
	if err != nil {
		log.Printf("Neuspelo pretvaranje poruke u JSON: %v", err)
		return err
	}

	message := &amqp.Message{
		Value: body,
	}

	sender, err := session.NewSender(ctx, address, &amqp.SenderOptions{
		SettlementMode: amqp.SenderSettleModeUnsettled.Ptr(),
	})
	if err != nil {
		log.Printf("Neuspelo kreiranje sender-a: %v", err)
		return err
	}
	defer sender.Close(ctx)

	err = sender.Send(ctx, message, nil)

	if err != nil {
		log.Printf("Neuspesan send: %v", err)
		return err
	}

	return nil
}

func listen(ctx context.Context, address string, handler func(context.Context, *amqp.Receiver, *amqp.Message), handlerErr func(context.Context, *amqp.Receiver, error)) {
	reciever, err := session.NewReceiver(ctx, address, &amqp.ReceiverOptions{
		Durability: amqp.DurabilityUnsettledState,
	})
	if err != nil {
		log.Fatalf("Neuspelo kreiranje receiver-a za listener %v: %v", address, err)
	}
	defer reciever.Close(ctx)

	for true {
		responseMessage, err := reciever.Receive(ctx, nil)
		if err != nil {
			handlerErr(ctx, reciever, err)
			continue
		}

		go handler(ctx, reciever, responseMessage)
	}
}
