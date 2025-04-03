package securities

import (
	"banka1.com/db"
	"banka1.com/types"
	"log"
	"time"
)

func LoadSecurities() {

	settlementDate := time.Now().AddDate(0, 0, 2).Format("2006-01-02")

	security := types.Security{
		Ticker:         "AAPL",
		Name:           "Apple Inc.",
		Type:           "Stock",
		Exchange:       "NASDAQ",
		LastPrice:      178.56,
		AskPrice:       179.00,
		BidPrice:       178.50,
		Volume:         123456789,
		SettlementDate: &settlementDate,
		StrikePrice:    nil,
		OptionType:     nil,
		UserID:         3,
	}

	if err := db.DB.Create(&security).Error; err != nil {
		log.Println("Failed to insert security:", err)
		return
	}

	log.Println("Security inserted successfully!")
}
