package stocks

import (
	"fmt"
	"strings"
	"time"

	"banka1.com/db"
	"banka1.com/listings/finhub"
	"banka1.com/types"
)

// API response structures
type TimeSeriesResponse struct {
	MetaData   map[string]string            `json:"Meta Data"`
	TimeSeries map[string]map[string]string `json:"Time Series (Daily)"`
}

// LoadStockFromAPI loads a single stock from Alpha Vantage API
func LoadStockFromAPI(ticker string, exchangeMic string) error {

	stockData, err := finhub.GetStockData(ticker)
	if err != nil {
		return fmt.Errorf("failed to fetch stock data: %w", err)
	}

	// Debug logging
	fmt.Printf("Trying to find exchange with MIC code: %s\n", exchangeMic)

	// Find exchange

	// Parse values
	price := stockData.Pc
	high := stockData.H
	low := stockData.L
	// volume := strconv.ParseInt(mostRecentData["5. volume"], 10, 64)
	lastRefresh := time.Now()

	// Begin transaction
	tx := db.DB.Begin()

	// Create or update listing
	var listing types.Listing
	if err := tx.Where("ticker = ?", ticker).First(&listing).Error; err != nil {
		// Create new listing
		listing = types.Listing{
			Ticker:       ticker,
			Name:         strings.Split(ticker, ".")[0] + " Inc.", // Simple name
			ExchangeID:   1,
			LastRefresh:  lastRefresh,
			Price:        *price,
			Ask:          *high,
			Bid:          *low,
			Type:         "Stock",
			ContractSize: 1,
		}

		fmt.Printf("Creating new listing for %s\n", ticker)
		if err := tx.Create(&listing).Error; err != nil {
			tx.Rollback()
			return fmt.Errorf("failed to create listing: %w", err)
		}
	} else {
		// Update existing
		listing.LastRefresh = lastRefresh
		listing.Price = *price
		listing.Ask = *high
		listing.Bid = *low

		fmt.Printf("Updating existing listing for %s (ID: %d)\n", ticker, listing.ID)
		if err := tx.Save(&listing).Error; err != nil {
			tx.Rollback()
			return fmt.Errorf("failed to update listing: %w", err)
		}
	}

	// Create or update stock info
	var stock types.Stock
	if err := tx.Where("listing_id = ?", listing.ID).First(&stock).Error; err != nil {
		stock = types.Stock{
			ListingID:         listing.ID,
			OutstandingShares: 1000000, // Default value
			DividendYield:     0.01,    // Default value
		}

		fmt.Printf("Creating stock details for listing ID %d\n", listing.ID)
		if err := tx.Create(&stock).Error; err != nil {
			tx.Rollback()
			return fmt.Errorf("failed to create stock details: %w", err)
		}
	}

	// Add historical price data (last 30 days)
	// count := 0
	// for dateStr, priceData := range data.TimeSeries {
	// 	date, _ := time.Parse("2006-01-02", dateStr)
	// 	dailyPrice, _ := strconv.ParseFloat(priceData["4. close"], 64)
	// 	dailyHigh, _ := strconv.ParseFloat(priceData["2. high"], 64)
	// 	dailyLow, _ := strconv.ParseFloat(priceData["3. low"], 64)
	// 	dailyVolume, _ := strconv.ParseInt(priceData["5. volume"], 10, 64)

	// 	var existing types.ListingDailyPriceInfo
	// 	if tx.Where("listing_id = ? AND date = ?", listing.ID, date).First(&existing).Error == nil {
	// 		continue
	// 	}

	// 	// Calculate change (simplified)
	// 	change := 0.0
	// 	if count > 0 {
	// 		prevDate := ""
	// 		for d := range data.TimeSeries {
	// 			if d < dateStr && (prevDate == "" || d > prevDate) {
	// 				prevDate = d
	// 			}
	// 		}
	// 		if prevDate != "" {
	// 			prevPrice, _ := strconv.ParseFloat(data.TimeSeries[prevDate]["4. close"], 64)
	// 			change = dailyPrice - prevPrice
	// 		}
	// 	}

	// 	// Create price record
	// 	priceInfo := types.ListingDailyPriceInfo{
	// 		ListingID: listing.ID,
	// 		Date:      date,
	// 		Price:     dailyPrice,
	// 		High:      dailyHigh,
	// 		Low:       dailyLow,
	// 		Change:    change,
	// 		Volume:    dailyVolume,
	// 	}

	// 	fmt.Printf("Adding price history for %s on %s\n", ticker, dateStr)
	// 	if err := tx.Create(&priceInfo).Error; err != nil {
	// 		tx.Rollback()
	// 		return fmt.Errorf("failed to create price history: %w", err)
	// 	}

	// 	count++
	// 	if count >= 30 {
	// 		break
	// 	}
	// }

	if err := tx.Commit().Error; err != nil {
		return fmt.Errorf("failed to commit transaction: %w", err)
	}

	fmt.Printf("Successfully loaded stock data for %s\n", ticker)
	return nil
}

func LoadDefaultStocks() {
	// Define stocks with their exchanges

	stocksData, err := finhub.GetAllStock()
	if err != nil {
		fmt.Printf("Failed to fetch stock data: %v\n", err)
		return
	}

	for _, exchange := range []string{"XNAS"} {
		var count int64
		db.DB.Model(&types.Exchange{}).Where("mic_code = ?", exchange).Count(&count)
		if count == 0 {
			fmt.Printf("WARNING: Exchange %s doesn't exist in the database!\n", exchange)
		} else {
			fmt.Printf("Exchange %s found in database\n", exchange)
		}
	}

	fmt.Println("------------------------------")

	for ticker, exchange := range stocksData {
		fmt.Printf("Loading %s on %s...\n", ticker, exchange)
		if err := LoadStockFromAPI(ticker, exchange); err != nil {
			fmt.Printf("Error loading %s: %v\n", ticker, err)
		} else {
			fmt.Printf("Successfully loaded %s\n", ticker)
		}
		time.Sleep(15 * time.Second) // Respect API rate limits
	}
}
