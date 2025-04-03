package tax

import (
	"banka1.com/db"
	"banka1.com/middlewares"
	"banka1.com/types"
	"github.com/gofiber/fiber/v2"
	"log"
	"time"
)

func GetTaxForAllUsers(c *fiber.Ctx) error {
	rows, err := db.DB.Raw(`WITH max_created_at AS (SELECT user_id, MAX(created_at) AS c FROM tax GROUP BY user_id)
SELECT user_id, taxable_profit, tax_amount, is_paid, actuary.id IS NOT NULL
FROM tax LEFT JOIN actuary USING (user_id)
WHERE month_year = (SELECT MAX(month_year) FROM tax)
AND created_at = (SELECT c FROM max_created_at WHERE max_created_at.user_id = tax.user_id)
AND NOT is_paid;`).Rows()
	defer rows.Close()
	if err != nil {
		return c.Status(400).JSON(types.Response{
			Success: false,
			Error:   "Neuspeo zahtev: " + err.Error(),
		})
	}
	responses := make([]types.TaxResponse, 0)
	for rows.Next() {
		var response types.TaxResponse
		err := rows.Scan(&response.UserID, &response.TaxableProfit, &response.TaxAmount, &response.IsPaid, &response.IsActuary)
		if err != nil {
			return c.Status(500).JSON(types.Response{
				Success: false,
				Error:   "Greska prilikom citanja redova iz baze: " + err.Error(),
			})
		}
		responses = append(responses, response)
	}
	return c.JSON(types.Response{
		Success: true,
		Data:    responses,
	})
}

func RunTax(c *fiber.Ctx) error {
	return c.Status(500).JSON(types.Response{
		Success: false,
		Error:   "Nije implementirano.",
	})
}

func GetAggregatedTax(c *fiber.Ctx) error {
	year := time.Now().Format("2006")
	yearMonth := time.Now().Format("2006-01")

	paidMap := make(map[uint]float64)
	unpaidMap := make(map[uint]float64)
	actuaryMap := make(map[uint]bool)

	// === PRVI UPIT: PAID THIS YEAR ===
	rowsPaid, err := db.DB.Raw(`
SELECT user_id, COALESCE(SUM(tax_amount), 0) 
FROM tax 
WHERE is_paid = 1 AND substr(created_at, 1, 4) = ?
GROUP BY user_id`, year).Rows()
	if err != nil {
		log.Println("RAW SELECT error:", err)
	}
	defer rowsPaid.Close()

	for rowsPaid.Next() {
		var uid uint
		var tax float64
		if err := rowsPaid.Scan(&uid, &tax); err == nil {
			paidMap[uid] = tax
		} else {
			log.Println("Scan error:", err)
		}
	}

	// === DRUGI UPIT: UNPAID ===
	rowsUnpaid, err := db.DB.Raw(`
	SELECT user_id, tax_amount
	FROM tax
	WHERE is_paid = 0
		AND substr(created_at, 1, 7) = ?
	GROUP BY user_id `, yearMonth).Rows()
	if err != nil {
		log.Println("[ERROR] Unpaid query error:", err)
		return c.Status(500).JSON(types.Response{
			Success: false,
			Error:   "Greška u UNPAID upitu: " + err.Error(),
		})
	}
	defer rowsUnpaid.Close()

	for rowsUnpaid.Next() {
		var userID uint
		var tax float64
		if err := rowsUnpaid.Scan(&userID, &tax); err == nil {
			unpaidMap[userID] += tax
		} else {
			log.Println("[ERROR] Scan UNPAID:", err)
		}
	}

	// === TREĆI UPIT: ACTUARIES ===
	rowsActuary, err := db.DB.Raw(`SELECT user_id FROM actuary`).Rows()
	if err != nil {
		return c.Status(500).JSON(types.Response{Success: false, Error: "Greška u ACTUARY upitu: " + err.Error()})
	}
	defer rowsActuary.Close()

	for rowsActuary.Next() {
		var userID uint
		if err := rowsActuary.Scan(&userID); err == nil {
			actuaryMap[userID] = true
		}
	}

	// === KOMBINUJ SVE ===
	userSet := make(map[uint]bool)
	for id := range paidMap {
		userSet[id] = true
	}
	for id := range unpaidMap {
		userSet[id] = true
	}

	var responses []types.AggregatedTaxResponse
	for userID := range userSet {
		response := types.AggregatedTaxResponse{
			UserID:          userID,
			PaidThisYear:    paidMap[userID],
			UnpaidThisMonth: unpaidMap[userID],
			IsActuary:       actuaryMap[userID],
		}
		responses = append(responses, response)
	}

	return c.JSON(types.Response{
		Success: true,
		Data:    responses,
	})
}

func InitRoutes(app *fiber.App) {
	app.Get("/tax", middlewares.Auth, GetTaxForAllUsers)
	app.Post("/tax/run", middlewares.Auth, middlewares.DepartmentCheck("SUPERVISOR"), RunTax)
	app.Get("/tax/dashboard", middlewares.Auth, GetAggregatedTax)
}
