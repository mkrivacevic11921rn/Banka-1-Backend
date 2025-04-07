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
	portfolio3 := types.Portfolio{
		UserID:        4,
		SecurityID:    2, // MSFT
		Quantity:      150,
		PurchasePrice: 199.99,
		PublicCount:   20,
	}

	portfolio4 := types.Portfolio{
		UserID:        4,
		SecurityID:    1, // MSFT
		Quantity:      80,
		PurchasePrice: 299.99,
		PublicCount:   30,
	}

	portfolio5 := types.Portfolio{
		UserID:        3,
		SecurityID:    3, // MSFT
		Quantity:      80,
		PurchasePrice: 299.99,
		PublicCount:   30,
	}

	if err := db.DB.FirstOrCreate(&portfolio3, types.Portfolio{
		UserID:     portfolio3.UserID,
		SecurityID: portfolio3.SecurityID,
	}).Error; err != nil {
		log.Println("Greška pri dodavanju portfolio3:", err)
	} else {
		log.Println("Portfolio3 uspešno dodat")
	}
	if err := db.DB.FirstOrCreate(&portfolio4, types.Portfolio{
		UserID:     portfolio4.UserID,
		SecurityID: portfolio4.SecurityID,
	}).Error; err != nil {
		log.Println("Greška pri dodavanju portfolio4:", err)
	} else {
		log.Println("Portfolio3 uspešno dodat")
	}
	if err := db.DB.FirstOrCreate(&portfolio5, types.Portfolio{
		UserID:     portfolio5.UserID,
		SecurityID: portfolio5.SecurityID,
	}).Error; err != nil {
		log.Println("Greška pri dodavanju portfolio5:", err)
	} else {
		log.Println("Portfolio3 uspešno dodat")
	}
}
