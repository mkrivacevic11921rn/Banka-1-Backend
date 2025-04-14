package dto

type PortfolioSecurityDTO struct {
	SecurityID   uint    `json:"securityId"`
	Ticker       string  `json:"ticker"`
	ID           uint    `json:"security_id"`
	Type         string  `json:"type"`
	Symbol       string  `json:"symbol"`
	Amount       int     `json:"amount"`
	Price        float64 `json:"price"`
	Profit       float64 `json:"profit"`
	LastModified int64   `json:"last_modified"`
	Public       int     `json:"public"`
}
