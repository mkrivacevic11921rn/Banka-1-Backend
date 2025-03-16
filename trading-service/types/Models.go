package types

type Actuary struct {
	ID           uint    `gorm:"primaryKey"`
	UserID       uint    `gorm:"uniqueIndex;not null"`
	Role         string  `gorm:"type:text;not null"`
	LimitAmount  float64 `gorm:"default:null"`  // Samo za agente
	UsedLimit    float64 `gorm:"default:0"`     // Samo za agente, resetuje se dnevno
	NeedApproval bool    `gorm:"default:false"` // Da li orderi agenta trebaju supervizorsko odobrenje
}

type Security struct {
	ID             uint    `gorm:"primaryKey"`
	Ticker         string  `gorm:"unique;not null"`
	Name           string  `gorm:"not null"`
	Type           string  `gorm:"type:text;not null"`
	Exchange       string  `gorm:"not null"`
	LastPrice      float64 `gorm:"not null"`
	AskPrice       float64 `gorm:"default:null"`
	BidPrice       float64 `gorm:"default:null"`
	Volume         int64   `gorm:"default:0"`
	SettlementDate *string `gorm:"default:null"` // Samo za futures i opcije
}

type Order struct {
	ID             uint     `gorm:"primaryKey"`
	UserID         uint     `gorm:"not null"`
	SecurityID     uint     `gorm:"not null"`
	OrderType      string   `gorm:"type:text;not null"`
	Quantity       int      `gorm:"not null"`
	ContractSize   int      `gorm:"default:1"`
	PricePerUnit   float64  `gorm:"default:null"` // Samo za limit i stop order-e
	Direction      string   `gorm:"type:text;not null"`
	Status         string   `gorm:"type:text;default:'pending'"`
	ApprovedBy     *uint    `gorm:"default:null"` // Supervizor koji je odobrio order
	IsDone         bool     `gorm:"default:false"`
	LastModified   int64    `gorm:"autoUpdateTime"`
	RemainingParts *int     `gorm:"default:null"`
	AfterHours     bool     `gorm:"default:false"`
	User           uint     `gorm:"foreignKey:UserID"`
	Security       Security `gorm:"foreignKey:SecurityID"`
	ApprovedByUser *uint    `gorm:"foreignKey:ApprovedBy"`
}

type OTCTrade struct {
	ID           uint     `gorm:"primaryKey"`
	SellerID     uint     `gorm:"not null"`
	BuyerID      *uint    `gorm:"default:null"` // NULL dok se ne nađe kupac
	SecurityID   uint     `gorm:"not null"`
	Quantity     int      `gorm:"not null"`
	PricePerUnit float64  `gorm:"not null"`
	Status       string   `gorm:"type:text;default:'pending'"`
	CreatedAt    int64    `gorm:"autoCreateTime"`
	Seller       uint     `gorm:"foreignKey:SellerID"`
	Buyer        *uint    `gorm:"foreignKey:BuyerID"`
	Security     Security `gorm:"foreignKey:SecurityID"`
}

type Portfolio struct {
	ID            uint     `gorm:"primaryKey"`
	UserID        uint     `gorm:"not null"`
	SecurityID    uint     `gorm:"not null"`
	Quantity      int      `gorm:"not null"`
	PurchasePrice float64  `gorm:"not null"`
	CreatedAt     int64    `gorm:"autoCreateTime"`
	User          uint     `gorm:"foreignKey:UserID"`
	Security      Security `gorm:"foreignKey:SecurityID"`
}

type Tax struct {
	ID            uint    `gorm:"primaryKey"`
	UserID        uint    `gorm:"not null"`
	MonthYear     string  `gorm:"not null"` // Format: YYYY-MM
	TaxableProfit float64 `gorm:"not null"`
	TaxAmount     float64 `gorm:"not null"`
	IsPaid        bool    `gorm:"default:false"`
	CreatedAt     int64   `gorm:"autoCreateTime"`
	User          uint    `gorm:"foreignKey:UserID"`
}

type Exchange struct {
	ID        uint   `gorm:"primaryKey"`
	Name      string `gorm:"not null"`
	Acronym   string `gorm:"not null"`
	MicCode   string `gorm:"unique;not null"`
	Country   string `gorm:"not null"`
	Currency  string `gorm:"not null"`
	Timezone  string `gorm:"not null"`
	WorkHours string `gorm:"type:json;not null"` // JSON format radnih sati
}
