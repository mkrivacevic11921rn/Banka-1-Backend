package main

import (
	"banka1.com/controllers"
	"fmt"
	fiberSwagger "github.com/swaggo/fiber-swagger"
	"os"
	"time"

	"banka1.com/cron"

	// options "banka1.com/listings/options"
	"banka1.com/middlewares"

	"banka1.com/listings/futures"
	"banka1.com/routes"

	"banka1.com/controllers"
	"banka1.com/db"
	_ "banka1.com/docs"
	"banka1.com/exchanges"
	"banka1.com/listings/finhub"
	"banka1.com/listings/forex"
	"banka1.com/listings/futures"
	"banka1.com/listings/stocks"
	"banka1.com/orders"
	"banka1.com/tax"
	"banka1.com/types"
	"github.com/gofiber/fiber/v2"
	"github.com/joho/godotenv"

	"log"
)

//	@title			Trading Service
//	@version		1.0
//	@description	Trading Service API

//	@host		localhost:3000
//	@BasePath	/

// @securityDefinitions.apikey	BearerAuth
// @in							header
// @name						Authorization
// @description				Unesite token. Primer: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
func main() {

	err := godotenv.Load()
	if err != nil {
		panic("Error loading .env file")
	}

	db.Init()
	cron.StartScheduler()

	err = exchanges.LoadDefaultExchanges()
	if err != nil {
		log.Printf("Warning: Failed to load exchanges: %v", err)
	}

	func() {
		log.Println("Starting to load default stocks...")
		stocks.LoadDefaultStocks()
		log.Println("Finished loading default stocks")
	}()

	func() {
		log.Println("Starting to load default forex pairs...")
		forex.LoadDefaultForexPairs()
		log.Println("Finished loading default forex pairs")
	}()

	func() {
		log.Println("Starting to load default futures...")
		err = futures.LoadDefaultFutures()
		if err != nil {
			log.Printf("Warning: Failed to load futures: %v", err)
		}
		log.Println("Finished loading default futures")
	}()

	//func() {
	//	log.Println("Starting to load default options...")
	//	err = options.LoadAllOptions()
	//	if err != nil {
	//		log.Printf("Warning: Failed to load options: %v", err)
	//	}
	//	log.Println("Finished loading default options")
	//}()

	func() {
		log.Println("Starting to load default securities...")
		LoadSecurities()
		log.Println("Finished loading default securities")
	}()

	func() {
		log.Println("Starting to load default taxes...")
		LoadTax()
		log.Println("Finished loading default taxes")
	}()

	app := fiber.New()

	app.Use(func(c *fiber.Ctx) error {
		c.Set("Access-Control-Allow-Origin", "*")
		c.Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE")
		return c.Next()
	})

	app.Get("/", middlewares.Auth, middlewares.DepartmentCheck("AGENT"), func(c *fiber.Ctx) error {
		response := types.Response{
			Success: true,
			Data:    "Hello, World!",
			Error:   "",
		}
		return c.JSON(response)
	})

	app.Get("/health", func(c *fiber.Ctx) error {
		return c.SendStatus(200)
	})

	controllers.InitActuaryRoutes(app)
	controllers.InitOrderRoutes(app)
	controllers.InitSecuritiesRoutes(app)
	controllers.InitExchangeRoutes(app)
	controllers.InitStockRoutes(app)
	controllers.InitForexRoutes(app)
	controllers.InitFutureRoutes(app)
	controllers.InitTaxRoutes(app)
	controllers.InitOptionsRoutes(app)

	// svaki put kad menjate swagger dokumentaciju (komentari iznad funkcija u controllerima, uradite "swag init" da bi se azuriralo)
	app.Get("/swagger/*", fiberSwagger.WrapHandler)

	ticker := time.NewTicker(5000 * time.Millisecond)
	done := make(chan bool)

	go func() {
		for {
			select {
			case <-done:
				return
			case <-ticker.C:
				checkUncompletedOrders()
			}
		}
	}()
	port := os.Getenv("LISTEN_PATH")
	log.Printf("Swagger UI available at http://localhost%s/swagger/index.html", port)
	log.Fatal(app.Listen(port))
	ticker.Stop()
	done <- true
}

func checkUncompletedOrders() {
	var undoneOrders []types.Order

	fmt.Println("Proveravanje neizvršenih naloga...")

	db.DB.Where("status = ? AND is_done = ?", "approved", false).Find(&undoneOrders)
	fmt.Printf("Pronadjeno %v neizvršenih naloga\n", len(undoneOrders))
	previousLength := -1

	for len(undoneOrders) > 0 && previousLength != len(undoneOrders) {
		fmt.Printf("Preostalo još %v neizvršenih naloga\n", len(undoneOrders))
		for _, order := range undoneOrders {
			if orders.CanExecuteAny(order) {
				orders.MatchOrder(order)
				break
			}
		}
		previousLength = len(undoneOrders)
		db.DB.Where("status = ? AND is_done = ?", "approved", false).Find(&undoneOrders)
	}
}

func getSecurities() func(c *fiber.Ctx) error {
	return func(c *fiber.Ctx) error {
		var listings []types.Listing
		if result := db.DB.Preload("Exchange").Find(&listings); result.Error != nil {
			return c.Status(500).JSON(types.Response{
				Success: false,
				Data:    nil,
				Error:   "Failed to fetch securities: " + result.Error.Error(),
			})
		}
		var securities []types.Security
		for _, listing := range listings {
			security, err := listingToSecurity(&listing)
			if err != nil || security == nil {
				return c.Status(500).JSON(types.Response{
					Success: false,
					Data:    nil,
					Error:   "Failed to convert listing to security: " + err.Error(),
				})
			}
			securities = append(securities, *security)
		}

		return c.JSON(types.Response{
			Success: true,
			Data:    securities,
			Error:   "",
		})
	}
}

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

func listingToSecurity(l *types.Listing) (*types.Security, error) {
	var security types.Security
	previousClose := getPreviousCloseForListing(l.ID)
	switch l.Type {
	case "Stock":
		{
			security = types.Security{
				ID:            l.ID,
				Ticker:        l.Ticker,
				Name:          l.Name,
				Type:          l.Type,
				Exchange:      l.Exchange.Name,
				LastPrice:     float64(l.Price),
				AskPrice:      float64(l.Ask),
				BidPrice:      float64(l.Bid),
				Volume:        int64(l.ContractSize * 10),
				ContractSize:  int64(l.ContractSize),
				PreviousClose: previousClose,
			}
		}
	case "Forex":
		{
			security = types.Security{
				ID:            l.ID,
				Ticker:        l.Ticker,
				Name:          l.Name,
				Type:          l.Type,
				Exchange:      l.Exchange.Name,
				LastPrice:     float64(l.Price),
				AskPrice:      float64(l.Ask),
				BidPrice:      float64(l.Bid),
				Volume:        int64(l.ContractSize * 10),
				ContractSize:  int64(l.ContractSize),
				PreviousClose: previousClose,
			}
		}
	case "Future":
		{
			var future types.FuturesContract
			if result := db.DB.Where("listing_id = ?", l.ID).First(&future); result.Error != nil {
				return nil, result.Error
			}
			settlementDate := future.SettlementDate.Format("2006-01-02")
			security = types.Security{
				ID:             l.ID,
				Ticker:         l.Ticker,
				Name:           l.Name,
				Type:           l.Type,
				Exchange:       l.Exchange.Name,
				LastPrice:      float64(l.Price),
				AskPrice:       float64(l.Ask),
				BidPrice:       float64(l.Bid),
				Volume:         int64(l.ContractSize * 10),
				SettlementDate: &settlementDate,
				ContractSize:   int64(l.ContractSize),
				PreviousClose:  previousClose,
			}
		}
	case "Option":
		{
			var option types.Option
			if result := db.DB.Where("listing_id = ?", l.ID).First(&option); result.Error != nil {
				return nil, result.Error
			}
			settlementDate := option.SettlementDate.Format("2006-01-02")
			security = types.Security{
				ID:             l.ID,
				Ticker:         l.Ticker,
				Name:           l.Name,
				Type:           l.Type,
				Exchange:       l.Exchange.Name,
				LastPrice:      float64(l.Price),
				AskPrice:       float64(l.Ask),
				BidPrice:       float64(l.Bid),
				Volume:         int64(l.ContractSize * 10),
				StrikePrice:    &option.StrikePrice,
				OptionType:     &option.OptionType,
				SettlementDate: &settlementDate,
				ContractSize:   int64(l.ContractSize),
				PreviousClose:  previousClose,
			}

		}

	}
	return &security, nil
}

func getPreviousCloseForListing(listingID uint) float64 {
	var dailyInfo types.ListingDailyPriceInfo

	yesterday := time.Now().AddDate(0, 0, -1)

	err := db.DB.
		Where("listing_id = ? AND DATE(date) = ?", listingID, yesterday.Format("2006-01-02")).
		Order("date DESC").
		First(&dailyInfo).Error

	if err == nil {
		return dailyInfo.Price
	}

	err = db.DB.
		Where("listing_id = ?", listingID).
		Order("date DESC").
		First(&dailyInfo).Error

	if err == nil {
		return dailyInfo.Price
	}

	return 0
}
