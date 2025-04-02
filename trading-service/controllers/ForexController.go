package controllers

import (
	"banka1.com/db"
	"banka1.com/types"
	"github.com/gofiber/fiber/v2"
	"time"
)

type ForexController struct {
}

func NewForexController() *ForexController {
	return &ForexController{}
}

// GetAllForex godoc
//
//	@Summary		Preuzimanje svih forex parova
//	@Description	Vraća listu svih listinga koji predstavljaju forex valutne parove.
//	@Tags			Forex
//	@Produce		json
//	@Success		200	{object}	types.Response{data=[]types.Listing}	"Lista svih forex parova"
//	@Failure		500	{object}	types.Response							"Interna greška servera pri preuzimanju forex parova"
//	@Router			/forex [get]
func (fc *ForexController) GetAllForex(c *fiber.Ctx) error {
	var listings []types.Listing

	if result := db.DB.Preload("Exchange").Where("type = ?", "Forex").Find(&listings); result.Error != nil {
		return c.Status(500).JSON(types.Response{
			Success: false,
			Data:    nil,
			Error:   "Failed to fetch stocks: " + result.Error.Error(),
		})
	}

	return c.JSON(types.Response{
		Success: true,
		Data:    listings,
		Error:   "",
	})
}

// GetForexByPair godoc
//
//	@Summary		Preuzimanje forex para po valutama
//	@Description	Vraća osnovne podatke (Listing) i detalje (ForexPair) za forex par specificiran osnovnom i kvotnom valutom.
//	@Tags			Forex
//	@Produce		json
//	@Param			base	path		string																		true	"Osnovna valuta (ISO kod)"	example(EUR)
//	@Param			quote	path		string																		true	"Kvotna valuta (ISO kod)"	example(USD)
//	@Success		200		{object}	types.Response{data=object{listing=types.Listing,details=types.ForexPair}}	"Detalji forex para"
//	@Failure		404		{object}	types.Response																"Forex par nije pronađen"
//	@Failure		500		{object}	types.Response																"Interna greška servera pri preuzimanju detalja forex para"
//	@Router			/forex/{base}/{quote} [get]
func (fc *ForexController) GetForexByPair(c *fiber.Ctx) error {

	base := c.Params("base")
	quote := c.Params("quote")
	ticker := base + "/" + quote

	var listing types.Listing
	if result := db.DB.Preload("Exchange").Where("ticker = ? AND type = ?", ticker, "Forex").First(&listing); result.Error != nil {
		return c.Status(404).JSON(types.Response{
			Success: false,
			Data:    nil,
			Error:   "Forex pair not found with ticker: " + ticker,
		})
	}

	var forexPair types.ForexPair
	if result := db.DB.Preload("Listing.Exchange").Where("listing_id = ?", listing.ID).First(&forexPair); result.Error != nil {
		return c.Status(500).JSON(types.Response{
			Success: false,
			Data:    nil,
			Error:   "Failed to fetch stock details: " + result.Error.Error(),
		})
	}

	return c.JSON(types.Response{
		Success: true,
		Data: map[string]interface{}{
			"listing": listing,
			"details": forexPair,
		},
		Error: "",
	})
}

// GetForexHistoryRange godoc
//
//	@Summary		Istorijski podaci za forex par u vremenskom opsegu
//	@Description	Preuzima dnevne istorijske podatke za forex par u vremenskom opsegu.
//	@Tags			Forex
//	@Produce		json
//	@Param			base		path		string												true	"Osnovna valuta"											example(EUR)
//	@Param			quote		path		string												true	"Kvotna valuta"												example(USD)
//	@Param			startDate	query		string												false	"Početni datum (YYYY-MM-DD). Podrazumevano pre 30 dana."	example(2023-09-27)	Format(date)
//	@Param			endDate		query		string												false	"Krajnji datum (YYYY-MM-DD). Podrazumevano današnji dan."	example(2023-10-27)	Format(date)
//	@Success		200			{object}	types.Response{data=[]types.ListingDailyPriceInfo}	"Lista istorijskih podataka"
//	@Failure		400			{object}	types.Response										"Neispravan format datuma"
//	@Failure		404			{object}	types.Response										"Forex par nije pronađen"
//	@Failure		500			{object}	types.Response										"Greška pri preuzimanju istorijskih podataka iz baze"
//	@Router			/forex/{base}/{quote}/history [get]
func (f *ForexController) GetForexHistoryRange(c *fiber.Ctx) error {
	base := c.Params("base")
	quote := c.Params("quote")
	ticker := base + "/" + quote
	startDateStr := c.Query("startDate", "")
	endDateStr := c.Query("endDate", "")
	var listing types.Listing
	if result := db.DB.Where("ticker = ? AND type = ?", ticker, "Forex").First(&listing); result.Error != nil {
		return c.Status(404).JSON(types.Response{
			Success: false,
			Data:    nil,
			Error:   "Stock not found with ticker: " + ticker,
		})
	}

	var startDate, endDate time.Time
	var err error

	if startDateStr != "" {
		startDate, err = time.Parse("2006-01-02", startDateStr)
		if err != nil {
			return c.Status(400).JSON(types.Response{
				Success: false,
				Data:    nil,
				Error:   "Invalid startDate format. Use YYYY-MM-DD",
			})
		}
	} else {
		// Default to 30 days ago
		startDate = time.Now().AddDate(0, 0, -30)
	}

	if endDateStr != "" {
		endDate, err = time.Parse("2006-01-02", endDateStr)
		if err != nil {
			return c.Status(400).JSON(types.Response{
				Success: false,
				Data:    nil,
				Error:   "Invalid endDate format. Use YYYY-MM-DD",
			})
		}
	} else {
		// Default to today
		endDate = time.Now()
	}

	// Fetch historical price data
	var history []types.ListingDailyPriceInfo
	if result := db.DB.Where("listing_id = ? AND date BETWEEN ? AND ?",
		listing.ID, startDate, endDate).Order("date").Find(&history); result.Error != nil {
		return c.Status(500).JSON(types.Response{
			Success: false,
			Data:    nil,
			Error:   "Failed to fetch historical price data: " + result.Error.Error(),
		})
	}
	return c.JSON(types.Response{
		Success: true,
		Data:    history,
		Error:   "",
	})
}

func InitForexRoutes(app *fiber.App) {
	forexController := NewForexController()

	app.Get("/forex", forexController.GetAllForex)
	app.Get("/forex/:base/:quote", forexController.GetForexByPair)
	app.Get("/forex/:base/:quote/history", forexController.GetForexHistoryRange)
}
