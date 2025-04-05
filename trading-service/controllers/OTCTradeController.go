package controllers

import (
	"banka1.com/db"
	"banka1.com/middlewares"
	"banka1.com/types"
	"fmt"
	"github.com/go-playground/validator/v10"
	"github.com/gofiber/fiber/v2"
	"time"
)

type UpdateOTCTradeRequest struct {
	Quantity       int     `json:"quantity" validate:"required,gt=0"`
	PricePerUnit   float64 `json:"price_per_unit" validate:"required,gt=0"`
	Premium        float64 `json:"premium" validate:"required,gte=0"`
	SettlementDate string  `json:"settlement_date" validate:"required"`
}

type OTCTradeController struct {
	validator *validator.Validate
}

func NewOTCTradeController() *OTCTradeController {
	return &OTCTradeController{
		validator: validator.New(),
	}
}

type CreateOTCTradeRequest struct {
	PortfolioID    uint    `json:"portfolio_id" validate:"required"`
	Quantity       int     `json:"quantity" validate:"required,gt=0"`
	PricePerUnit   float64 `json:"price_per_unit" validate:"required,gt=0"`
	Premium        float64 `json:"premium" validate:"required,gte=0"`
	SettlementDate string  `json:"settlement_date" validate:"required"`
}

func (c *OTCTradeController) CreateOTCTrade(ctx *fiber.Ctx) error {
	var req CreateOTCTradeRequest

	if err := ctx.BodyParser(&req); err != nil {
		return ctx.Status(fiber.StatusBadRequest).JSON(types.Response{
			Success: false,
			Error:   "Nevalidan JSON format",
		})
	}

	if err := c.validator.Struct(req); err != nil {
		return ctx.Status(fiber.StatusBadRequest).JSON(types.Response{
			Success: false,
			Error:   err.Error(),
		})
	}

	settlementDate, err := time.Parse("2006-01-02", req.SettlementDate)
	if err != nil {
		return ctx.Status(fiber.StatusBadRequest).JSON(types.Response{
			Success: false,
			Error:   "Nevalidan format datuma. Očekivan format: YYYY-MM-DD",
		})
	}

	userID := uint(ctx.Locals("user_id").(float64))

	var portfolio types.Portfolio
	if err := db.DB.First(&portfolio, req.PortfolioID).Error; err != nil {
		return ctx.Status(fiber.StatusNotFound).JSON(types.Response{
			Success: false,
			Error:   "Portfolio nije pronađen",
		})
	}

	if portfolio.UserID == userID {
		return ctx.Status(fiber.StatusForbidden).JSON(types.Response{
			Success: false,
			Error:   "Ne možete praviti OTC ponudu za svoje akcije",
		})
	}

	if portfolio.PublicCount < req.Quantity {
		return ctx.Status(fiber.StatusBadRequest).JSON(types.Response{
			Success: false,
			Error:   "Nedovoljno javno dostupnih akcija u portfoliju",
		})
	}

	otcTrade := types.OTCTrade{
		PortfolioID:  portfolio.ID,
		SecurityId:   portfolio.SecurityID,
		SellerID:     portfolio.UserID,
		BuyerID:      &userID,
		Quantity:     req.Quantity,
		PricePerUnit: req.PricePerUnit,
		Premium:      req.Premium,
		SettlementAt: settlementDate,
		CreatedAt:    time.Now().Unix(),
		LastModified: time.Now().Unix(),
		ModifiedBy:   &userID,
		Status:       "pending",
	}

	if err := db.DB.Create(&otcTrade).Error; err != nil {
		return ctx.Status(fiber.StatusInternalServerError).JSON(types.Response{
			Success: false,
			Error:   "Greška prilikom čuvanja OTC ponude",
		})
	}

	return ctx.Status(fiber.StatusCreated).JSON(types.Response{
		Success: true,
		Data:    otcTrade.ID,
	})
}

func (c *OTCTradeController) CounterOfferOTCTrade(ctx *fiber.Ctx) error {
	id := ctx.Params("id")
	var req UpdateOTCTradeRequest

	if err := ctx.BodyParser(&req); err != nil {
		return ctx.Status(fiber.StatusBadRequest).JSON(types.Response{
			Success: false,
			Error:   "Nevalidan JSON format",
		})
	}

	if err := c.validator.Struct(req); err != nil {
		return ctx.Status(fiber.StatusBadRequest).JSON(types.Response{
			Success: false,
			Error:   err.Error(),
		})
	}

	userID := uint(ctx.Locals("user_id").(float64))

	settlementDate, err := time.Parse("2006-01-02", req.SettlementDate)
	if err != nil {
		return ctx.Status(fiber.StatusBadRequest).JSON(types.Response{
			Success: false,
			Error:   "Nevalidan format datuma. Očekivan format: YYYY-MM-DD",
		})
	}

	var trade types.OTCTrade
	if err := db.DB.Preload("Portfolio").First(&trade, id).Error; err != nil {
		return ctx.Status(fiber.StatusNotFound).JSON(types.Response{
			Success: false,
			Error:   "Ponuda nije pronađena",
		})
	}

	if trade.ModifiedBy != nil && *trade.ModifiedBy == userID {
		return ctx.Status(fiber.StatusForbidden).JSON(types.Response{
			Success: false,
			Error:   "Ne možete uzastopno menjati ponudu",
		})
	}

	var portfolio types.Portfolio
	if err := db.DB.First(&portfolio, trade.PortfolioID).Error; err != nil {
		return ctx.Status(fiber.StatusInternalServerError).JSON(types.Response{
			Success: false,
			Error:   "Greška pri proveri portfolija prodavca",
		})
	}
	if portfolio.PublicCount < req.Quantity {
		return ctx.Status(fiber.StatusBadRequest).JSON(types.Response{
			Success: false,
			Error:   "Prodavac više nema dovoljno raspoloživih akcija",
		})
	}

	trade.Quantity = req.Quantity
	trade.PricePerUnit = req.PricePerUnit
	trade.Premium = req.Premium
	trade.SettlementAt = settlementDate
	trade.LastModified = time.Now().Unix()
	trade.ModifiedBy = &userID
	trade.Status = "pending"

	if err := db.DB.Save(&trade).Error; err != nil {
		return ctx.Status(fiber.StatusInternalServerError).JSON(types.Response{
			Success: false,
			Error:   "Greška prilikom čuvanja kontraponude",
		})
	}

	return ctx.Status(fiber.StatusOK).JSON(types.Response{
		Success: true,
		Data:    trade,
	})
}

func (c *OTCTradeController) AcceptOTCTrade(ctx *fiber.Ctx) error {
	id := ctx.Params("id")
	userID := uint(ctx.Locals("user_id").(float64))

	var trade types.OTCTrade
	if err := db.DB.Preload("Portfolio").First(&trade, id).Error; err != nil {
		return ctx.Status(fiber.StatusNotFound).JSON(types.Response{
			Success: false,
			Error:   "Ponuda nije pronađena",
		})
	}

	if trade.SellerID != userID {
		return ctx.Status(fiber.StatusForbidden).JSON(types.Response{
			Success: false,
			Error:   "Nemate pravo da prihvatite ovu ponudu",
		})
	}

	if trade.Status == "accepted" || trade.Status == "executed" {
		return ctx.Status(fiber.StatusBadRequest).JSON(types.Response{
			Success: false,
			Error:   "Ova ponuda je već prihvaćena ili realizovana",
		})
	}

	var portfolio types.Portfolio
	if err := db.DB.First(&portfolio, trade.PortfolioID).Error; err != nil {
		return ctx.Status(fiber.StatusInternalServerError).JSON(types.Response{
			Success: false,
			Error:   "Greška pri dohvatanju portfolija",
		})
	}

	var existingContracts []types.OptionContract
	if err := db.DB.
		Where("seller_id = ? AND portfolio_id = ? AND is_exercised = false AND status = ?", userID, portfolio.ID, "active").
		Find(&existingContracts).Error; err != nil {
		return ctx.Status(fiber.StatusInternalServerError).JSON(types.Response{
			Success: false,
			Error:   "Greška pri proveri postojećih ugovora",
		})
	}

	usedQuantity := 0
	for _, contract := range existingContracts {
		usedQuantity += contract.Quantity
	}

	if usedQuantity+trade.Quantity > portfolio.PublicCount {
		return ctx.Status(fiber.StatusBadRequest).JSON(types.Response{
			Success: false,
			Error:   "Nemate dovoljno raspoloživih akcija za prihvatanje ove ponude",
		})
	}

	trade.Status = "accepted"
	trade.LastModified = time.Now().Unix()
	trade.ModifiedBy = &userID
	if err := db.DB.Save(&trade).Error; err != nil {
		return ctx.Status(fiber.StatusInternalServerError).JSON(types.Response{
			Success: false,
			Error:   "Greška pri ažuriranju ponude",
		})
	}

	// TODO: Poziv ka banking-servisu za prenos premije

	contract := types.OptionContract{
		OTCTradeID:   trade.ID,
		BuyerID:      *trade.BuyerID,
		SellerID:     trade.SellerID,
		PortfolioID:  trade.PortfolioID,
		Quantity:     trade.Quantity,
		StrikePrice:  trade.PricePerUnit,
		Premium:      trade.Premium,
		SettlementAt: trade.SettlementAt,
		IsExercised:  false,
		CreatedAt:    time.Now().Unix(),
	}
	if err := db.DB.Create(&contract).Error; err != nil {
		return ctx.Status(fiber.StatusInternalServerError).JSON(types.Response{
			Success: false,
			Error:   "Greška pri kreiranju ugovora",
		})
	}

	return ctx.Status(fiber.StatusOK).JSON(types.Response{
		Success: true,
		Data:    fmt.Sprintf("Ponuda uspešno prihvaćena,kreiran ugovor: %d", contract.ID),
	})
}

func (c *OTCTradeController) ExecuteOptionContract(ctx *fiber.Ctx) error {
	id := ctx.Params("id")
	userID := uint(ctx.Locals("user_id").(float64))

	var contract types.OptionContract
	if err := db.DB.First(&contract, id).Error; err != nil {
		return ctx.Status(fiber.StatusNotFound).JSON(types.Response{
			Success: false,
			Error:   "Ugovor nije pronađen",
		})
	}

	if contract.BuyerID != userID {
		return ctx.Status(fiber.StatusForbidden).JSON(types.Response{
			Success: false,
			Error:   "Nemate pravo da izvršite ovaj ugovor",
		})
	}

	if contract.IsExercised {
		return ctx.Status(fiber.StatusBadRequest).JSON(types.Response{
			Success: false,
			Error:   "Ovaj ugovor je već iskorišćen",
		})
	}

	if contract.SettlementAt.Before(time.Now()) {
		return ctx.Status(fiber.StatusBadRequest).JSON(types.Response{
			Success: false,
			Error:   "Ugovor je istekao",
		})
	}

	// TODO: SAGA transakcija
	// 1. Rezervacija novca kupca
	// 2. Rezervacija akcija prodavca
	// 3. Transfer novca
	// 4. Transfer vlasništva
	// 5. Finalizacija

	now := time.Now().Unix()
	contract.IsExercised = true
	contract.ExercisedAt = &now
	//contract.Status = "completed"

	if err := db.DB.Save(&contract).Error; err != nil {
		return ctx.Status(fiber.StatusInternalServerError).JSON(types.Response{
			Success: false,
			Error:   "Greška prilikom čuvanja statusa ugovora",
		})
	}

	if err := db.DB.Model(&types.OTCTrade{}).Where("id = ?", contract.OTCTradeID).Update("status", "completed").Error; err != nil {
		return ctx.Status(fiber.StatusInternalServerError).JSON(types.Response{
			Success: false,
			Error:   "Greška pri ažuriranju OTC ponude",
		})
	}

	return ctx.Status(fiber.StatusOK).JSON(types.Response{
		Success: true,
		Data:    "Ugovor uspešno realizovan",
	})
}

func InitOTCTradeRoutes(app *fiber.App) {
	otcController := NewOTCTradeController()
	otc := app.Group("/otctrade", middlewares.Auth)

	otc.Post("/offer", otcController.CreateOTCTrade)
	otc.Put("/offer/:id/counter", otcController.CounterOfferOTCTrade)
	otc.Put("/offer/:id/accept", otcController.AcceptOTCTrade)
	otc.Put("/option/:id/execute", otcController.ExecuteOptionContract)
}
