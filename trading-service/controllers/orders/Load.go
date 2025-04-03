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
