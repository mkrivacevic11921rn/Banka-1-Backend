package tax

import (
	"banka1.com/db"
	"banka1.com/types"
	"log"
	"time"
)

func LoadTax() {

	monthYear := time.Now().Format("2006-01")

	taxData := types.Tax{
		UserID:        3,
		MonthYear:     monthYear,
		TaxableProfit: 50000.00,
		TaxAmount:     15000.00,
		IsPaid:        false,
		CreatedAt:     time.Now().Format("2006-01-02"),
	}

	if err := db.DB.Create(&taxData).Error; err != nil {
		log.Println("Failed to insert tax:", err)
		return
	}

	log.Println("Tax record inserted successfully!")
}
