package stocks

import (
    "encoding/json"
    "fmt"
    "io"
    "net/http"
    "os"
    "strconv"
    "strings"
    "time"

    "banka1.com/db"
    "banka1.com/types"
)

// API response structures
type TimeSeriesResponse struct {
    MetaData   map[string]string             `json:"Meta Data"`
    TimeSeries map[string]map[string]string  `json:"Time Series (Daily)"`
}

// LoadStockFromAPI loads a single stock from Alpha Vantage API
func LoadStockFromAPI(ticker string, exchangeMic string) error {
    apiKey := os.Getenv("ALPHA_KEY")
    apiURL := fmt.Sprintf("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&outputsize=compact&apikey=%s", ticker, apiKey)
    
    // Fetch data
    resp, err := http.Get(apiURL)
    if err != nil || resp.StatusCode != 200 {
        return fmt.Errorf("API request failed: %v", err)
    }
    defer resp.Body.Close()
    
    // Parse response
    body, _ := io.ReadAll(resp.Body)
    var data TimeSeriesResponse
    if err := json.Unmarshal(body, &data); err != nil {
        return fmt.Errorf("failed to parse API response: %w", err)
    }
    
    // Check for valid data
    if len(data.TimeSeries) == 0 || data.MetaData == nil {
        return fmt.Errorf("no data returned for %s", ticker)
    }
    
    // Debug logging
    fmt.Printf("Trying to find exchange with MIC code: %s\n", exchangeMic)
    
    // Find exchange
    var exchange types.Exchange
    result := db.DB.Where("mic_code = ?", exchangeMic).First(&exchange)
    if result.Error != nil {
        // Debug: List all exchanges in the database
        var exchanges []types.Exchange
        db.DB.Find(&exchanges)
        fmt.Println("Available exchanges in database:")
        for _, ex := range exchanges {
            fmt.Printf("- %s (MIC: %s)\n", ex.Name, ex.MicCode)
        }
        return fmt.Errorf("exchange %s not found: %w", exchangeMic, result.Error)
    }
    
    fmt.Printf("Found exchange: %s (ID: %d)\n", exchange.Name, exchange.ID)
    
    // Get most recent data
    var mostRecentDate string
    var mostRecentData map[string]string
    for date, priceData := range data.TimeSeries {
        if mostRecentDate == "" || date > mostRecentDate {
            mostRecentDate = date
            mostRecentData = priceData
        }
    }
    
    // Parse values
    price, _ := strconv.ParseFloat(mostRecentData["4. close"], 64)
    high, _ := strconv.ParseFloat(mostRecentData["2. high"], 64)
    low, _ := strconv.ParseFloat(mostRecentData["3. low"], 64)
    volume, _ := strconv.ParseInt(mostRecentData["5. volume"], 10, 64)
    lastRefresh, _ := time.Parse("2006-01-02", mostRecentDate)
    
    // Begin transaction
    tx := db.DB.Begin()
    
    // Create or update listing
    var listing types.Listing
    if err := tx.Where("ticker = ?", ticker).First(&listing).Error; err != nil {
        // Create new listing
        listing = types.Listing{
            Ticker:       ticker,
            Name:         strings.Split(ticker, ".")[0] + " Inc.", // Simple name
            ExchangeID:   exchange.ID,
            LastRefresh:  lastRefresh,
            Price:        price,
            Ask:          high,
            Bid:          low,
            Volume:       volume,
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
        listing.Price = price
        listing.Ask = high
        listing.Bid = low
        listing.Volume = volume
        
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
    count := 0
    for dateStr, priceData := range data.TimeSeries {
        date, _ := time.Parse("2006-01-02", dateStr)
        dailyPrice, _ := strconv.ParseFloat(priceData["4. close"], 64)
        dailyHigh, _ := strconv.ParseFloat(priceData["2. high"], 64)
        dailyLow, _ := strconv.ParseFloat(priceData["3. low"], 64)
        dailyVolume, _ := strconv.ParseInt(priceData["5. volume"], 10, 64)
        
        var existing types.ListingDailyPriceInfo
        if tx.Where("listing_id = ? AND date = ?", listing.ID, date).First(&existing).Error == nil {
            continue
        }
        
        // Calculate change (simplified)
        change := 0.0
        if count > 0 {
            prevDate := ""
            for d := range data.TimeSeries {
                if d < dateStr && (prevDate == "" || d > prevDate) {
                    prevDate = d
                }
            }
            if prevDate != "" {
                prevPrice, _ := strconv.ParseFloat(data.TimeSeries[prevDate]["4. close"], 64)
                change = dailyPrice - prevPrice
            }
        }
        
        // Create price record
        priceInfo := types.ListingDailyPriceInfo{
            ListingID: listing.ID,
            Date:      date,
            Price:     dailyPrice,
            High:      dailyHigh,
            Low:       dailyLow,
            Change:    change,
            Volume:    dailyVolume,
        }
        
        fmt.Printf("Adding price history for %s on %s\n", ticker, dateStr)
        if err := tx.Create(&priceInfo).Error; err != nil {
            tx.Rollback()
            return fmt.Errorf("failed to create price history: %w", err)
        }
        
        count++
        if count >= 30 {
            break 
        }
    }
    
    if err := tx.Commit().Error; err != nil {
        return fmt.Errorf("failed to commit transaction: %w", err)
    }
    
    fmt.Printf("Successfully loaded stock data for %s\n", ticker)
    return nil
}

// LoadDefaultStocks loads all specified stocks
func LoadDefaultStocks() {
    // Define stocks with their exchanges
    stocksData := map[string]string{
        "AAPL":     "XNAS",
        "MSFT":     "XNAS",
        "NVDA":     "XNAS",
        "AMZN":     "XNAS",
        "GOOG":     "XNAS",
        "META":     "XNAS",
        "MNG.LON":  "XLME",
        "TSCO.LON": "XLME",
        "MRO.LON":  "XLME",
        "SHOP.TRT": "XDRK",
        "CLS.TRT":  "XDRK",
    }
    
    for _, exchange := range []string{"XNAS", "XLME", "XDRK"} {
        var count int64
        db.DB.Model(&types.Exchange{}).Where("mic_code = ?", exchange).Count(&count)
        if count == 0 {
            fmt.Printf("WARNING: Exchange %s doesn't exist in the database!\n", exchange)
        } else {
            fmt.Printf("Exchange %s found in database\n", exchange)
        }
    }
    
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