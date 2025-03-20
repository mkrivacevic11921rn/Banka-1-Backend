package types

import (
	"time"
)

type Listing struct {
	ID           uint      `gorm:"primaryKey"`
	Ticker       string    `gorm:"unique;not null"`
	Name         string    `gorm:"not null"`
	ExchangeID   uint      `gorm:"not null"`
	Exchange     Exchange  `gorm:"foreignKey:ExchangeID"`
	LastRefresh  time.Time `gorm:"not null"`
	Price        float32   `gorm:"not null"`
	Ask          float32   `gorm:"not null"`
	Bid          float32   `gorm:"not null"`
	Type         string    `gorm:"not null"` // "Stock", "Forex", "Future", "Option"
	Subtype      string    `gorm:"null"`     // "ETF", "Common Stock", "Call Option", "Put Option"
	ContractSize int       `gorm:"not null"`
	CreatedAt    time.Time `gorm:"autoCreateTime"`
	UpdatedAt    time.Time `gorm:"autoUpdateTime"`
}

type ListingDailyPriceInfo struct {
	ID        uint      `gorm:"primaryKey"`
	ListingID uint      `gorm:"not null"`
	Date      time.Time `gorm:"not null"`
	Price     float64   `gorm:"not null"`
	High      float64   `gorm:"not null"`
	Low       float64   `gorm:"not null"`
	Change    float64   `gorm:"not null"`
	Volume    int64     `gorm:"not null"`
	Listing   Listing   `gorm:"foreignKey:ListingID"`
}

type Stock struct {
	ID                uint    `gorm:"primaryKey"`
	ListingID         uint    `gorm:"unique;not null"`
	OutstandingShares int64   `gorm:"not null"`
	DividendYield     float64 `gorm:"not null"`
	Listing           Listing `gorm:"foreignKey:ListingID" json:"-" gorm:"-"`
}

type ForexPair struct {
	ID            uint    `gorm:"primaryKey"`
	ListingID     uint    `gorm:"unique;not null"`
	BaseCurrency  string  `gorm:"not null"`
	QuoteCurrency string  `gorm:"not null"`
	ExchangeRate  float64 `gorm:"not null"`
	Liquidity     string  `gorm:"not null"` // "High", "Medium", "Low"
	Listing       Listing `gorm:"foreignKey:ListingID"`
}

type FuturesContract struct {
	ID             uint      `gorm:"primaryKey"`
	ListingID      uint      `gorm:"unique;not null"`
	ContractUnit   string    `gorm:"not null"` // "Barrel", "Kilogram", "Liter"
	SettlementDate time.Time `gorm:"not null"`
	Listing        Listing   `gorm:"foreignKey:ListingID"`
}

type Option struct {
	ID             uint      `gorm:"primaryKey"`
	ListingID      uint      `gorm:"not null"` // Veza sa osnovnom akcijom
	Listing        Listing   `gorm:"foreignKey:ListingID"`
	OptionType     string    `gorm:"not null"`             // "Call" ili "Put"
	StrikePrice    float64   `gorm:"not null"`             // Ugovorena cena
	ImpliedVol     float64   `gorm:"not null"`             // Nestabilnost cene
	OpenInterest   int64     `gorm:"not null"`             // Broj otvorenih ugovora
	SettlementDate time.Time `gorm:"not null"`             // Datum isteka opcije
	ContractSize   int       `gorm:"not null;default:100"` // Standardno za akcije
	CreatedAt      time.Time `gorm:"autoCreateTime"`
	UpdatedAt      time.Time `gorm:"autoUpdateTime"`
}
