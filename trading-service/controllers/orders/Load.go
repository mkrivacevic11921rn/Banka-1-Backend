package orders

import (
	"banka1.com/db"
	"banka1.com/types"
	"log"
)

func LoadOrders() {
	order1 := types.Order{
		UserID:       3,
		AccountID:    1,
		SecurityID:   1,
		OrderType:    "Market",
		Quantity:     10,
		ContractSize: 1,
		Direction:    "Buy",
		Status:       "Approved",
		IsDone:       true,
	}

	order2 := types.Order{
		UserID:       3,
		AccountID:    1,
		SecurityID:   2,
		OrderType:    "Market",
		Quantity:     5,
		ContractSize: 1,
		Direction:    "Buy",
		Status:       "Approved",
		IsDone:       true,
	}

	if err := db.DB.FirstOrCreate(&order1, types.Order{
		UserID:     order1.UserID,
		SecurityID: order1.SecurityID,
		Direction:  order1.Direction,
	}).Error; err != nil {
		log.Println("Error while adding order1:", err)
	} else {
		log.Printf("Order succesfully added to Security ID %d (order1)\n", order1.SecurityID)
	}

	if err := db.DB.FirstOrCreate(&order2, types.Order{
		UserID:     order2.UserID,
		SecurityID: order2.SecurityID,
		Direction:  order2.Direction,
	}).Error; err != nil {
		log.Println("Error while adding order2:", err)
	} else {
		log.Printf("Order succesfully added to Security ID %d (order2)\n", order2.SecurityID)
	}
}

func LoadPortfolios() {
	portfolio1 := types.Portfolio{
		UserID:        3,
		SecurityID:    1, // AAPL
		Quantity:      20,
		PurchasePrice: 190.00,
		PublicCount:   10,
		CreatedAt:     1743892011,
	}

	portfolio2 := types.Portfolio{
		UserID:        5,
		SecurityID:    2, // MSFT
		Quantity:      15,
		PurchasePrice: 360.00,
		PublicCount:   10,
		CreatedAt:     1743892011,
	}

	if err := db.DB.FirstOrCreate(&portfolio1, types.Portfolio{
		UserID:     portfolio1.UserID,
		SecurityID: portfolio1.SecurityID,
	}).Error; err != nil {
		log.Println("Greška pri dodavanju portfolio1:", err)
	} else {
		log.Println("Portfolio1 uspešno dodat")
	}

	if err := db.DB.FirstOrCreate(&portfolio2, types.Portfolio{
		UserID:     portfolio2.UserID,
		SecurityID: portfolio2.SecurityID,
	}).Error; err != nil {
		log.Println("Greška pri dodavanju portfolio2:", err)
	} else {
		log.Println("Portfolio2 uspešno dodat")
	}

}
