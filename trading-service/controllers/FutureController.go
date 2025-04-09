package controllers

import (
	"banka1.com/db"
	"banka1.com/types"
	"github.com/gofiber/fiber/v2"
)

type FutureController struct {
}

func NewFutureController() *FutureController {
	return &FutureController{}
}

// GetAllFutures godoc
//
//	@Summary		Preuzimanje svih future-a
//	@Description	Vraća listu svih listinga koji predstavljaju future ugovore.
//	@Tags			Futures
//	@Produce		json
//	@Success		200	{object}	types.Response{data=[]types.Listing}	"Lista svih future-a"
//	@Failure		500	{object}	types.Response							"Interna greška servera pri preuzimanju future-a"
//	@Router			/future [get]
func (fc *FutureController) GetAllFutures(c *fiber.Ctx) error {
	var listings []types.Listing

	if result := db.DB.Preload("Exchange").Where("type = ?", "Future").Find(&listings); result.Error != nil {
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

// GetFutureByTicker godoc
//
//	@Summary		Preuzimanje future-a po tikeru
//	@Description	Vraća osnovne podatke (Listing) i detalje (FuturesContract) za future specificiran tikerom.
//	@Tags			Futures
//	@Produce		json
//	@Param			ticker	path		string																				true	"Tiker (simbol) future-a"	example(ESZ3)
//	@Success		200		{object}	types.Response{data=object{listing=types.Listing,details=types.FuturesContract}}	"Detalji future-a"
//	@Failure		404		{object}	types.Response																		"Future sa datim tikerom nije pronađen"
//	@Failure		500		{object}	types.Response																		"Interna greška servera pri preuzimanju detalja future-a"
//	@Router			/future/{ticker} [get]
func (fc *FutureController) GetFutureByTicker(c *fiber.Ctx) error {
	ticker := c.Params("ticker")

	var listing types.Listing
	if result := db.DB.Preload("Exchange").Where("ticker = ? AND type = ?", ticker, "Future").First(&listing); result.Error != nil {
		return c.Status(404).JSON(types.Response{
			Success: false,
			Data:    nil,
			Error:   "Stock not found with ticker: " + ticker,
		})
	}

	var future types.FuturesContract
	if result := db.DB.Preload("Listing.Exchange").Where("listing_id = ?", listing.ID).First(&future); result.Error != nil {
		return c.Status(500).JSON(types.Response{
			Success: false,
			Data:    nil,
			Error:   "Failed to fetch future details: " + result.Error.Error(),
		})
	}

	return c.JSON(types.Response{
		Success: true,
		Data: map[string]interface{}{
			"listing": listing,
			"details": future,
		},
		Error: "",
	})
}

func InitFutureRoutes(app *fiber.App) {
	futureController := NewFutureController()

	app.Get("/future", futureController.GetAllFutures)
	app.Get("/future/:ticker", futureController.GetFutureByTicker)
}
