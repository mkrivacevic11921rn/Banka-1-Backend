package dto

type InterbankOTCOfferDTO struct {
	Stock          string          `json:"stock" validate:"required"`
	Quantity       int             `json:"quantity" validate:"required,gt=0"`
	PricePerUnit   float64         `json:"pricePerUnit" validate:"required,gt=0"`
	Premium        float64         `json:"premium" validate:"required,gte=0"`
	SettlementDate string          `json:"settlementDate" validate:"required"`
	Buyer          ForeignBankUser `json:"buyer"`
	Seller         ForeignBankUser `json:"seller"`
}

type ForeignBankUser struct {
	RoutingNumber int    `json:"routingNumber"`
	UserID        string `json:"userId"`
}
