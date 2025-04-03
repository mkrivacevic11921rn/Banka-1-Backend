package controllers

import (
	"banka1.com/db"
	"banka1.com/dto"
	"banka1.com/types"
	"github.com/gofiber/fiber/v2"
	"log"
)

type PortfolioController struct{}

func NewPortfolioController() *PortfolioController {
	return &PortfolioController{}
}

func (pc *PortfolioController) GetUserSecurities(c *fiber.Ctx) error {
	userId := c.Params("id")
	if userId == "" {
		return c.Status(400).JSON(types.Response{
			Success: false,
			Error:   "Nedostaje user ID",
		})
	}

	var portfolios []types.Portfolio
	if err := db.DB.Preload("Security").Where("user_id = ?", userId).Find(&portfolios).Error; err != nil {
		return c.Status(500).JSON(types.Response{
			Success: false,
			Error:   "Greška pri dohvatanju portfolija: " + err.Error(),
		})
	}

	var response []dto.PortfolioSecurityDTO
	for _, p := range portfolios {
		profit := (p.Security.LastPrice - p.PurchasePrice) * float64(p.Quantity)

		var lastMod int64
		err := db.DB.
			Table("order").
			Select("MAX(last_modified)").
			Where("user_id = ? AND security_id = ?", p.UserID, p.SecurityID).
			Scan(&lastMod).Error
		if err != nil || lastMod == 0 {
			log.Println("Error fetching lastModified:", err)
			lastMod = p.CreatedAt // fallback
		}

		item := dto.PortfolioSecurityDTO{
			Ticker:       p.Security.Ticker,
			Type:         p.Security.Type,
			Symbol:       p.Security.Ticker, // koristiš isto kao symbol
			Amount:       p.Quantity,
			Price:        p.PurchasePrice,
			Profit:       profit,
			LastModified: lastMod,
			Public:       p.Security.Exchange != "", // ako postoji exchange, smatra se javnim
		}
		response = append(response, item)
	}

	return c.JSON(types.Response{
		Success: true,
		Data:    response,
		Error:   "",
	})
}
