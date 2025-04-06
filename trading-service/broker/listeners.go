package broker

import (
	"context"
	"encoding/json"
	"log"

	"banka1.com/db"
	"banka1.com/types"

	"github.com/Azure/go-amqp"
)

func handle(ctx context.Context, reciever *amqp.Receiver, message *amqp.Message, makeObject func() any, handler func(any) any) {
	err := reciever.AcceptMessage(ctx, message)
	if err != nil {
		log.Printf("Neuspesno prihvatanje poruke: %v", err)
		return
	}

	if message.Properties.ReplyTo == nil {
		log.Printf("Poruka nema postavljenu ReplyTo adresu: %v", message)
		return
	}

	object := makeObject()

	err = json.Unmarshal(messageValueAsBytes(message), object)
	if err != nil {
		log.Printf("Neuspesno parsiranje poruke: %v", err)
		return
	}

	response := handler(object)

	err = send(*message.Properties.ReplyTo, response)
	if err != nil {
		log.Printf("Neuspesno slanje reply-a: %v", err)
		return
	}
}

func handleNoReply(ctx context.Context, reciever *amqp.Receiver, message *amqp.Message, makeObject func() any, handler func(any)) {
	err := reciever.AcceptMessage(ctx, message)
	if err != nil {
		log.Printf("Neuspesno prihvatanje poruke: %v", err)
		return
	}

	object := makeObject()

	err = json.Unmarshal(messageValueAsBytes(message), object)
	if err != nil {
		log.Printf("Neuspesno parsiranje poruke: %v", err)
		return
	}

	handler(object)
}

func defaultErrHandler(_ context.Context, _ *amqp.Receiver, err error) {
	log.Fatalf("Greska u primanju poruke: %v", err)
}

func wrap(makeObject func() any, handler func(any) any) func(context.Context, *amqp.Receiver, *amqp.Message) {
	return func(ctx context.Context, reciever *amqp.Receiver, message *amqp.Message) {
		handle(ctx, reciever, message, makeObject, handler)
	}
}

func wrapNoReply(makeObject func() any, handler func(any)) func(context.Context, *amqp.Receiver, *amqp.Message) {
	return func(ctx context.Context, reciever *amqp.Receiver, message *amqp.Message) {
		handleNoReply(ctx, reciever, message, makeObject, handler)
	}
}

func getActuary(id any) any {
	var actuary types.Actuary
	if db.DB.First(&actuary, "id = ?", *(id.(*int))).Error != nil {
		return nil
	}
	return &actuary
}

func StartListeners(ctx context.Context) {
	go listen(ctx, "get-actuary", wrap(func() any { var id int; return &id }, getActuary), defaultErrHandler)
}
