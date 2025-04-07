package broker

import (
	"context"
	"encoding/json"
	"fmt"
	"gorm.io/gorm"
	"log"
	"time"

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

func handleReliable(ctx context.Context, reciever *amqp.Receiver, message *amqp.Message, makeObject func() any, handler func(any) error) {
	object := makeObject()

	err := json.Unmarshal(messageValueAsBytes(message), object)
	if err != nil {
		log.Printf("Neuspesno parsiranje poruke: %v", err)
		return
	}

	for true {
		err = handler(object)
		if err == nil {
			break
		}
		log.Printf("Greska u procesiranju poruke %v, sledeci pokusaj za 5 sekundi. Greska: %v", message, err)
		time.Sleep(5 * time.Second)
	}

	err = reciever.AcceptMessage(ctx, message)
	if err != nil {
		log.Printf("Neuspesno prihvatanje poruke: %v", err)
		return
	}
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

func wrapReliable(makeObject func() any, handler func(any) error) func(context.Context, *amqp.Receiver, *amqp.Message) {
	return func(ctx context.Context, reciever *amqp.Receiver, message *amqp.Message) {
		handleReliable(ctx, reciever, message, makeObject, handler)
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

func handleOTCACK(data any) error {
	dto := data.(*types.OTCTransactionACKDTO)

	if dto.Failure {
		log.Println("Saga failed:", dto.Message)
		if err := rollbackOwnership(dto.Uid); err != nil {
			log.Printf("Rollback ownership error: %v", err)
		}
		return nil
	}

	log.Println("Saga progressed for", dto.Uid)

	if err := transferOwnership(dto.Uid); err != nil {
		log.Printf("Ownership transfer error: %v", err)
		_ = SendOTCTransactionFailure(dto.Uid, "Ownership transfer failed")
		return err
	}

	return nil
}

func transferOwnership(uid string) error {
	return db.DB.Transaction(func(tx *gorm.DB) error {
		var contract types.OptionContract
		if err := tx.Where("concat('OTC-', otc_trade_id, '-', exercised_at) = ?", uid).First(&contract).Error; err != nil {
			return fmt.Errorf("Neuspešno pronalaženje ugovora: %w", err)
		}

		var sellerPortfolio types.Portfolio
		if err := tx.First(&sellerPortfolio, contract.PortfolioID).Error; err != nil {
			return fmt.Errorf("Neuspešno pronalaženje portfolija prodavca: %w", err)
		}

		if sellerPortfolio.Quantity < contract.Quantity {
			return fmt.Errorf("Prodavac nema dovoljno akcija")
		}
		if sellerPortfolio.PublicCount < contract.Quantity {
			return fmt.Errorf("Prodavac nema dovoljno javno raspoloživih akcija")
		}

		sellerPortfolio.Quantity -= contract.Quantity
		sellerPortfolio.PublicCount -= contract.Quantity
		if sellerPortfolio.PublicCount < 0 {
			sellerPortfolio.PublicCount = 0
		}
		if err := tx.Save(&sellerPortfolio).Error; err != nil {
			return fmt.Errorf("Greška prilikom ažuriranja portfolija prodavca: %w", err)
		}

		buyerPortfolio := types.Portfolio{
			UserID:        contract.BuyerID,
			SecurityID:    contract.SecurityID,
			Quantity:      contract.Quantity,
			PurchasePrice: contract.StrikePrice,
			PublicCount:   0,
		}
		if err := tx.Create(&buyerPortfolio).Error; err != nil {
			return fmt.Errorf("Greška prilikom kreiranja portfolija kupca: %w", err)
		}

		return nil
	})
}

func rollbackOwnership(uid string) error {
	var contract types.OptionContract
	if err := db.DB.Where("concat('OTC-', otc_trade_id, '-', exercised_at) = ?", uid).First(&contract).Error; err != nil {
		return err
	}

	var sellerPortfolio types.Portfolio
	if err := db.DB.First(&sellerPortfolio, contract.PortfolioID).Error; err == nil {
		sellerPortfolio.Quantity += contract.Quantity
		sellerPortfolio.PublicCount += contract.Quantity
		if err := db.DB.Save(&sellerPortfolio).Error; err != nil {
			return err
		}
	}

	var buyerPortfolio types.Portfolio
	err := db.DB.Where("user_id = ? AND security_id = ?", contract.BuyerID, contract.SecurityID).First(&buyerPortfolio).Error
	if err == nil && buyerPortfolio.Quantity >= contract.Quantity {
		buyerPortfolio.Quantity -= contract.Quantity
		if buyerPortfolio.Quantity == 0 {
			if err := db.DB.Delete(&buyerPortfolio).Error; err != nil {
				return err
			}
		} else {
			if err := db.DB.Save(&buyerPortfolio).Error; err != nil {
				return err
			}
		}
	}

	return nil
}
func StartOTCListeners(ctx context.Context) {
	go listen(ctx, "otc-ack-trade", wrapReliable(func() any { return &types.OTCTransactionACKDTO{} }, handleOTCACK), defaultErrHandler)
}
