package main

import (
	"banka1.com/controllers/orders"
	"banka1.com/db"
	"bytes"
	"encoding/json"
	"fmt"
	"gorm.io/gorm"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"os"
	"strconv"
	"testing"
	"time"

	"banka1.com/types"

	"github.com/gofiber/fiber/v2"
	"github.com/stretchr/testify/assert"
)

var app *fiber.App

func TestMain(m *testing.M) {
	app = fiber.New()
	setupMockRoutes()
	go func() {
		if err := app.Listen(":8082"); err != nil {
			panic(err)
		}
	}()

	err := os.Setenv("BANKING_SERVICE", "http://localhost:8082")
	if err != nil {
		return
	}
	code := m.Run()
	os.Exit(code)
}

func setupMockRoutes() {
	app.Get("/", func(c *fiber.Ctx) error {
		return c.JSON(types.Response{Success: true, Data: "API is running"})
	})

	app.Post("/actuaries", func(c *fiber.Ctx) error {
		var body map[string]interface{}
		if err := c.BodyParser(&body); err != nil {
			return c.Status(400).JSON(types.Response{Success: false, Error: "Invalid request"})
		}
		return c.JSON(types.Response{Success: true, Data: map[string]interface{}{
			"id":           1,
			"userID":       body["userID"],
			"role":         body["role"],
			"limitAmount":  body["limitAmount"],
			"usedLimit":    body["usedLimit"],
			"needApproval": body["needApproval"],
		}})
	})

	app.Get("/actuaries/all", func(c *fiber.Ctx) error {
		return c.JSON(types.Response{Success: true, Data: []map[string]interface{}{
			{
				"id":           1,
				"userID":       101,
				"role":         "AGENT",
				"limitAmount":  100000,
				"usedLimit":    15000,
				"needApproval": true,
			},
			{
				"id":           2,
				"userID":       102,
				"role":         "SUPERVISOR",
				"limitAmount":  500000,
				"usedLimit":    50000,
				"needApproval": false,
			},
		}})
	})

	app.Get("/actuaries/filter", func(c *fiber.Ctx) error {
		role := c.Query("role")
		data := []map[string]interface{}{}
		if role == "AGENT" {
			data = append(data, map[string]interface{}{
				"id":           1,
				"userID":       101,
				"role":         "AGENT",
				"limitAmount":  100000,
				"usedLimit":    15000,
				"needApproval": true,
			})
		}
		return c.JSON(types.Response{Success: true, Data: data})
	})

	app.Put("/actuaries/:id", func(c *fiber.Ctx) error {
		return c.JSON(types.Response{Success: true, Data: map[string]interface{}{
			"id":           c.Params("id"),
			"userID":       101,
			"role":         "AGENT",
			"limitAmount":  150000,
			"usedLimit":    15000,
			"needApproval": false,
		}})
	})

	app.Get("/orders", func(c *fiber.Ctx) error {
		return c.JSON(types.Response{Success: true, Data: []map[string]interface{}{
			{
				"id":                1,
				"userID":            101,
				"accountID":         1001,
				"securityID":        1,
				"orderType":         "LIMIT",
				"quantity":          10,
				"contractSize":      1,
				"limitPricePerUnit": 100.00,
				"direction":         "BUY",
				"status":            "pending",
			},
			{
				"id":                2,
				"userID":            101,
				"accountID":         1001,
				"securityID":        1,
				"orderType":         "LIMIT",
				"quantity":          5,
				"contractSize":      1,
				"limitPricePerUnit": 110.00,
				"direction":         "SELL",
				"status":            "pending",
			},
		}})
	})

	app.Post("/orders", func(c *fiber.Ctx) error {
		var body map[string]interface{}
		if err := c.BodyParser(&body); err != nil {
			return c.Status(400).JSON(types.Response{Success: false, Error: "Invalid request"})
		}
		return c.JSON(types.Response{Success: true, Data: map[string]interface{}{
			"id":                3,
			"userID":            101,
			"accountID":         body["accountID"],
			"securityID":        body["securityID"],
			"orderType":         body["orderType"],
			"quantity":          body["quantity"],
			"contractSize":      body["contractSize"],
			"limitPricePerUnit": body["limitPricePerUnit"],
			"direction":         body["direction"],
			"status":            "pending",
		}})
	})

	app.Post("/orders/:id/approve", func(c *fiber.Ctx) error {
		return c.JSON(types.Response{Success: true, Data: map[string]interface{}{
			"id":                c.Params("id"),
			"userID":            101,
			"accountID":         1001,
			"securityID":        1,
			"orderType":         "LIMIT",
			"quantity":          50,
			"contractSize":      1,
			"limitPricePerUnit": 100.00,
			"direction":         "BUY",
			"status":            "approved",
		}})
	})

	app.Post("/orders/:id/decline", func(c *fiber.Ctx) error {
		return c.JSON(types.Response{Success: true, Data: map[string]interface{}{
			"id":                c.Params("id"),
			"userID":            101,
			"accountID":         1001,
			"securityID":        1,
			"orderType":         "LIMIT",
			"quantity":          30,
			"contractSize":      1,
			"limitPricePerUnit": 105.00,
			"direction":         "SELL",
			"status":            "declined",
		}})
	})

	app.Get("/exchanges", func(c *fiber.Ctx) error {
		return c.JSON(types.Response{Success: true, Data: []map[string]interface{}{
			{
				"id":        1,
				"name":      "Test Exchange",
				"acronym":   "TST",
				"micCode":   "TEST",
				"country":   "Test Country",
				"currency":  "USD",
				"timezone":  "UTC",
				"openTime":  "09:00",
				"closeTime": "17:00",
			},
		}})
	})

	app.Get("/securities", func(c *fiber.Ctx) error {
		return c.JSON(types.Response{Success: true, Data: []map[string]interface{}{
			{
				"id":        1,
				"ticker":    "TSLA",
				"name":      "Tesla Inc.",
				"type":      "STOCK",
				"exchange":  "TEST",
				"lastPrice": 100.00,
				"askPrice":  102.00,
				"bidPrice":  98.00,
				"volume":    500000,
			},
			{
				"id":        2,
				"ticker":    "AAPL",
				"name":      "Apple Inc.",
				"type":      "STOCK",
				"exchange":  "TEST",
				"lastPrice": 150.00,
				"askPrice":  150.50,
				"bidPrice":  149.50,
				"volume":    1000000,
			},
		}})
	})

	app.Post("/orders/execute/:token", func(c *fiber.Ctx) error {
		// Možeš i da proveravaš vrednosti ako ti treba
		fmt.Println("Poziv ka MOCK /orders/execute primljen sa token:", c.Params("token"))
		return c.SendStatus(200) // simulacija uspeha
	})

	app.Use(func(c *fiber.Ctx) error {
		if c.Get("X-Test-Skip-Auth") == "true" {
			return c.Next()
		}

		if c.Get("X-Test-Auth") == "true" {
			c.Locals("user", map[string]interface{}{
				"id":         101,
				"department": c.Get("X-Test-Department"),
			})
			return c.Next()
		}

		return c.Status(401).JSON(types.Response{Success: false, Error: "Unauthorized"})
	})
}

func makeRequest(t *testing.T, method, url string, body interface{}, headers map[string]string) *http.Response {
	var reqBody []byte
	var err error

	if body != nil {
		reqBody, err = json.Marshal(body)
		if err != nil {
			t.Fatalf("Failed to marshal JSON: %v", err)
		}
	}

	req := httptest.NewRequest(method, url, bytes.NewReader(reqBody))
	req.Header.Set("Content-Type", "application/json")

	for key, value := range headers {
		req.Header.Set(key, value)
	}

	resp, err := app.Test(req)
	if err != nil {
		t.Fatalf("Failed to test request: %v", err)
	}

	return resp
}

func parseResponse(t *testing.T, resp *http.Response) types.Response {
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		t.Fatalf("Failed to read response body: %v", err)
	}

	var result types.Response
	err = json.Unmarshal(body, &result)
	if err != nil {
		t.Fatalf("Failed to parse response JSON: %v", err)
	}

	return result
}

func TestCreateActuary(t *testing.T) {
	actuaryDTO := map[string]interface{}{
		"userID":       103,
		"role":         "AGENT",
		"limitAmount":  75000,
		"usedLimit":    0,
		"needApproval": true,
	}

	resp := makeRequest(t, "POST", "/actuaries", actuaryDTO, nil)
	assert.Equal(t, 200, resp.StatusCode)

	result := parseResponse(t, resp)
	assert.True(t, result.Success)

	data := result.Data.(map[string]interface{})
	assert.Equal(t, float64(103), data["userID"])
	assert.Equal(t, "AGENT", data["role"])
}

func TestGetAllActuaries(t *testing.T) {
	resp := makeRequest(t, "GET", "/actuaries/all", nil, nil)
	assert.Equal(t, 200, resp.StatusCode)

	result := parseResponse(t, resp)
	assert.True(t, result.Success)

	actuaries, ok := result.Data.([]interface{})
	if !ok {
		t.Fatalf("Expected array of actuaries")
	}

	assert.GreaterOrEqual(t, len(actuaries), 2)
}

func TestFilterActuaries(t *testing.T) {
	resp := makeRequest(t, "GET", "/actuaries/filter?role=AGENT", nil, nil)
	assert.Equal(t, 200, resp.StatusCode)

	result := parseResponse(t, resp)
	assert.True(t, result.Success)

	actuaries, ok := result.Data.([]interface{})
	if !ok {
		t.Fatalf("Expected array of actuaries")
	}

	assert.GreaterOrEqual(t, len(actuaries), 1)
}

func TestChangeAgentLimits(t *testing.T) {
	limitAmount := float64(150000)
	updateDTO := map[string]interface{}{
		"limitAmount": limitAmount,
		"resetLimit":  false,
	}

	resp := makeRequest(t, "PUT", "/actuaries/1", updateDTO, nil)
	assert.Equal(t, 200, resp.StatusCode)

	result := parseResponse(t, resp)
	assert.True(t, result.Success)

	data := result.Data.(map[string]interface{})
	assert.Equal(t, float64(150000), data["limitAmount"])
}

func TestGetOrders(t *testing.T) {
	resp := makeRequest(t, "GET", "/orders", nil, map[string]string{
		"X-Test-Skip-Auth": "true",
	})
	assert.Equal(t, 200, resp.StatusCode)

	result := parseResponse(t, resp)
	assert.True(t, result.Success)

	orders, ok := result.Data.([]interface{})
	if !ok {
		t.Fatalf("Expected array of orders")
	}

	assert.GreaterOrEqual(t, len(orders), 2)
}

func TestCreateOrder(t *testing.T) {
	limitPrice := 95.50
	orderRequest := map[string]interface{}{
		"securityID":        1,
		"accountID":         1001,
		"orderType":         "LIMIT",
		"quantity":          20,
		"limitPricePerUnit": limitPrice,
		"direction":         "BUY",
		"contractSize":      1,
	}

	resp := makeRequest(t, "POST", "/orders", orderRequest, map[string]string{
		"X-Test-Auth":       "true",
		"X-Test-Department": "AGENT",
	})
	assert.Equal(t, 200, resp.StatusCode)

	result := parseResponse(t, resp)
	assert.True(t, result.Success)

	data := result.Data.(map[string]interface{})
	assert.Equal(t, float64(1), data["securityID"])
	assert.Equal(t, "BUY", data["direction"])
	assert.Equal(t, "pending", data["status"])
}

func TestApproveOrder(t *testing.T) {
	limitPrice := 100.00
	orderRequest := map[string]interface{}{
		"securityID":        1,
		"accountID":         1001,
		"orderType":         "LIMIT",
		"quantity":          50,
		"limitPricePerUnit": limitPrice,
		"direction":         "BUY",
		"contractSize":      1,
	}

	resp := makeRequest(t, "POST", "/orders", orderRequest, map[string]string{
		"X-Test-Auth":       "true",
		"X-Test-Department": "AGENT",
	})

	result := parseResponse(t, resp)
	orderData := result.Data.(map[string]interface{})
	orderID := int(orderData["id"].(float64))

	resp = makeRequest(t, "POST", "/orders/"+strconv.Itoa(orderID)+"/approve", nil, map[string]string{
		"X-Test-Auth":       "true",
		"X-Test-Department": "SUPERVISOR",
	})
	assert.Equal(t, 200, resp.StatusCode)

	result = parseResponse(t, resp)
	assert.True(t, result.Success)

	data := result.Data.(map[string]interface{})
	assert.Equal(t, "approved", data["status"])
}

func TestDeclineOrder(t *testing.T) {
	limitPrice := 105.00
	orderRequest := map[string]interface{}{
		"securityID":        1,
		"accountID":         1001,
		"orderType":         "LIMIT",
		"quantity":          30,
		"limitPricePerUnit": limitPrice,
		"direction":         "SELL",
		"contractSize":      1,
	}

	resp := makeRequest(t, "POST", "/orders", orderRequest, map[string]string{
		"X-Test-Auth":       "true",
		"X-Test-Department": "AGENT",
	})

	result := parseResponse(t, resp)
	orderData := result.Data.(map[string]interface{})
	orderID := int(orderData["id"].(float64))

	resp = makeRequest(t, "POST", "/orders/"+strconv.Itoa(orderID)+"/decline", nil, map[string]string{
		"X-Test-Auth":       "true",
		"X-Test-Department": "SUPERVISOR",
	})
	assert.Equal(t, 200, resp.StatusCode)

	result = parseResponse(t, resp)
	assert.True(t, result.Success)

	data := result.Data.(map[string]interface{})
	assert.Equal(t, "declined", data["status"])
}

func TestAuthMiddleware(t *testing.T) {
	resp := makeRequest(t, "GET", "/", nil, nil)
	assert.Equal(t, 200, resp.StatusCode)

	resp = makeRequest(t, "GET", "/", nil, map[string]string{
		"X-Test-Auth":       "true",
		"X-Test-Department": "AGENT",
	})
	assert.Equal(t, 200, resp.StatusCode)
}

func TestGetExchanges(t *testing.T) {
	resp := makeRequest(t, "GET", "/exchanges", nil, nil)
	assert.Equal(t, 200, resp.StatusCode)

	result := parseResponse(t, resp)
	assert.True(t, result.Success)

	exchanges, ok := result.Data.([]interface{})
	if !ok {
		t.Fatalf("Expected array of exchanges")
	}

	assert.GreaterOrEqual(t, len(exchanges), 1)
}

func TestGetSecurities(t *testing.T) {
	resp := makeRequest(t, "GET", "/securities", nil, nil)
	assert.Equal(t, 200, resp.StatusCode)

	result := parseResponse(t, resp)
	assert.True(t, result.Success)

	securities, ok := result.Data.([]interface{})
	if !ok {
		t.Fatalf("Expected array of securities")
	}

	assert.GreaterOrEqual(t, len(securities), 1)
}

func TestCreateOrder_AllAONMarginCombinations(t *testing.T) {
	combinations := []struct {
		AON    bool
		Margin bool
	}{
		{false, false},
		{true, false},
		{false, true},
		{true, true},
	}

	for _, combo := range combinations {
		t.Run(fmt.Sprintf("AON_%v_Margin_%v", combo.AON, combo.Margin), func(t *testing.T) {
			orderRequest := map[string]interface{}{
				"user_id":       1,
				"account_id":    1001,
				"security_id":   1,
				"quantity":      10,
				"contract_size": 1,
				"direction":     "buy",
				"aon":           combo.AON,
				"margin":        combo.Margin,
			}

			resp := makeRequest(t, "POST", "/orders", orderRequest, map[string]string{
				"X-Test-Auth":       "true",
				"X-Test-Department": "AGENT",
			})

			assert.Equal(t, 200, resp.StatusCode, fmt.Sprintf("Expected 200 but got %d for AON=%v, Margin=%v", resp.StatusCode, combo.AON, combo.Margin))
		})
	}
}

func TestMatchOrderExecution(t *testing.T) {
	err := db.InitTestDatabase()
	if err != nil {
		t.Fatalf("InitTestDatabase failed: %v", err)
	}

	err1 := os.Setenv("BANKING_SERVICE", "http://localhost:8082")
	if err1 != nil {
		return
	}

	listing := types.Listing{
		ID:     7,
		Ticker: "MSFT",
		Type:   "Stock",
		Ask:    102.0,
		Bid:    98.0,
		Price:  100.0,
	}
	assert.NoError(t, db.DB.Create(&listing).Error)

	security := types.Security{
		ID:     7,
		Ticker: "MSFT",
		Name:   "Microsoft",
		Volume: 1000,
	}
	assert.NoError(t, db.DB.Create(&security).Error)

	// Dodaj početni PORTFOLIO za SELL korisnika (da bi mogao da prodaje)
	initialSellPortfolio := types.Portfolio{
		UserID:     3,
		SecurityID: 7,
		Quantity:   3,
	}
	assert.NoError(t, db.DB.Create(&initialSellPortfolio).Error)

	// Dodaj testni order i match order u bazu
	//price := 100.0
	sellOrder := types.Order{
		ID:             2001,
		UserID:         3,
		AccountID:      101,
		SecurityID:     7,
		Quantity:       3,
		RemainingParts: ptr(3),
		ContractSize:   1,
		Direction:      "sell",
		Status:         "approved",
		AON:            false,
		Margin:         false,
	}
	buyOrder := types.Order{
		ID:             2002,
		UserID:         6,
		AccountID:      21,
		SecurityID:     7,
		Quantity:       3,
		RemainingParts: ptr(3),
		ContractSize:   1,
		Direction:      "buy",
		Status:         "approved",
		AON:            false,
		Margin:         false,
	}

	assert.NoError(t, db.DB.Create(&sellOrder).Error)
	assert.NoError(t, db.DB.Create(&buyOrder).Error)

	// Ručno pozovi funkciju koju inače backend poziva
	orders.MatchOrder(buyOrder)

	time.Sleep(2 * time.Second)

	// Provera: BUY
	var updatedBuy types.Order
	assert.NoError(t, db.DB.First(&updatedBuy, buyOrder.ID).Error)
	assert.True(t, updatedBuy.IsDone)
	assert.Equal(t, 0, *updatedBuy.RemainingParts)

	// Provera: SELL
	var updatedSell types.Order
	assert.NoError(t, db.DB.First(&updatedSell, sellOrder.ID).Error)
	assert.True(t, updatedSell.IsDone)
	assert.Equal(t, 0, *updatedSell.RemainingParts)

	// Provera: SECURITY
	var updatedSecurity types.Security
	assert.NoError(t, db.DB.First(&updatedSecurity, security.ID).Error)
	assert.Equal(t, int64(997), updatedSecurity.Volume)

	// Provera: PORTFOLIO
	var portfolio types.Portfolio
	err = db.DB.Where("user_id = ? AND security_id = ?", buyOrder.UserID, buyOrder.SecurityID).
		First(&portfolio).Error
	assert.NoError(t, err)
	assert.Equal(t, 3, int(portfolio.Quantity))

	// Provera: portfolio za SELL korisnika umanjen
	var sellPortfolio types.Portfolio
	err = db.DB.Where("user_id = ? AND security_id = ?", sellOrder.UserID, sellOrder.SecurityID).
		First(&sellPortfolio).Error
	assert.Error(t, err) // očekujemo grešku jer ne postoji
	assert.Equal(t, gorm.ErrRecordNotFound, err)

}

func TestMatchOrderExecution_AONShouldNotExecute(t *testing.T) {
	err := db.InitTestDatabase()
	assert.NoError(t, err)

	err1 := os.Setenv("BANKING_SERVICE", "http://localhost:8082")
	if err1 != nil {
		return
	}

	// 🔹 Listing i security
	listing := types.Listing{
		ID:     10,
		Ticker: "TSLA",
		Type:   "Stock",
		Ask:    205.0,
		Bid:    200.0,
		Price:  202.0,
	}
	assert.NoError(t, db.DB.Create(&listing).Error)

	security := types.Security{
		ID:     10,
		Ticker: "TSLA",
		Name:   "Tesla",
		Volume: 1000,
	}
	assert.NoError(t, db.DB.Create(&security).Error)

	// 🔹 SELL korisnik ima 2
	assert.NoError(t, db.DB.Create(&types.Portfolio{
		UserID:     21,
		SecurityID: 10,
		Quantity:   2,
	}).Error)

	// 🔹 SELL order za 2
	sellOrder := types.Order{
		ID:             4001,
		UserID:         21,
		AccountID:      301,
		SecurityID:     10,
		Quantity:       2,
		RemainingParts: ptr(2),
		ContractSize:   1,
		Direction:      "sell",
		Status:         "approved",
		AON:            false,
		Margin:         false,
	}
	assert.NoError(t, db.DB.Create(&sellOrder).Error)

	// 🔹 AON BUY order za 5
	buyOrder := types.Order{
		ID:             4002,
		UserID:         22,
		AccountID:      302,
		SecurityID:     10,
		Quantity:       5,
		RemainingParts: ptr(5),
		ContractSize:   1,
		Direction:      "buy",
		Status:         "approved",
		AON:            true, // ❗ AON uključen
		Margin:         false,
	}
	assert.NoError(t, db.DB.Create(&buyOrder).Error)

	// 🔹 Matchuj AON BUY
	orders.MatchOrder(buyOrder)

	time.Sleep(2 * time.Second)

	// 🔍 BUY nije izvršen
	var updatedBuy types.Order
	assert.NoError(t, db.DB.First(&updatedBuy, buyOrder.ID).Error)
	assert.False(t, updatedBuy.IsDone)
	assert.Equal(t, 5, *updatedBuy.RemainingParts)

	// 🔍 SELL ostaje netaknut
	var updatedSell types.Order
	assert.NoError(t, db.DB.First(&updatedSell, sellOrder.ID).Error)
	assert.False(t, updatedSell.IsDone)
	assert.Equal(t, 2, *updatedSell.RemainingParts)

	// 🔍 Portfolio za BUY user ne postoji
	var portfolio types.Portfolio
	err = db.DB.Where("user_id = ? AND security_id = ?", buyOrder.UserID, buyOrder.SecurityID).
		First(&portfolio).Error
	assert.Equal(t, gorm.ErrRecordNotFound, err)

	// 🔍 Volume ostaje isti
	var updatedSecurity types.Security
	assert.NoError(t, db.DB.First(&updatedSecurity, security.ID).Error)
	assert.Equal(t, int64(1000), updatedSecurity.Volume)

	fmt.Println("✅ AON test uspešno prošao – order nije izvršen jer nije bilo moguće sve odjednom.")
}

func TestMatchOrderExecution_PartialFillWithoutAON(t *testing.T) {
	err := db.InitTestDatabase()
	assert.NoError(t, err)

	os.Setenv("BANKING_SERVICE", "http://localhost:8082")

	// 🔹 Listing i security
	listing := types.Listing{
		ID:     11,
		Ticker: "AMZN",
		Type:   "Stock",
		Ask:    155.0,
		Bid:    150.0,
		Price:  152.0,
	}
	assert.NoError(t, db.DB.Create(&listing).Error)

	security := types.Security{
		ID:     11,
		Ticker: "AMZN",
		Name:   "Amazon",
		Volume: 1000,
	}
	assert.NoError(t, db.DB.Create(&security).Error)

	// 🔹 SELL user ima 3 u portfoliju
	assert.NoError(t, db.DB.Create(&types.Portfolio{
		UserID:     31,
		SecurityID: 11,
		Quantity:   3,
	}).Error)

	// 🔹 SELL order za 3
	sellOrder := types.Order{
		ID:             5001,
		UserID:         31,
		AccountID:      601,
		SecurityID:     11,
		Quantity:       3,
		RemainingParts: ptr(3),
		ContractSize:   1,
		Direction:      "sell",
		Status:         "approved",
		AON:            false,
		Margin:         false,
	}
	assert.NoError(t, db.DB.Create(&sellOrder).Error)

	// 🔹 BUY order za 5 (više nego što ima na SELL strani)
	buyOrder := types.Order{
		ID:             5002,
		UserID:         32,
		AccountID:      602,
		SecurityID:     11,
		Quantity:       5,
		RemainingParts: ptr(5),
		ContractSize:   1,
		Direction:      "buy",
		Status:         "approved",
		AON:            false,
		Margin:         false,
	}
	assert.NoError(t, db.DB.Create(&buyOrder).Error)

	// 🔹 Matchuj
	orders.MatchOrder(buyOrder)

	time.Sleep(2 * time.Second)

	// ✅ SELL order – kompletno izvršen
	var updatedSell types.Order
	assert.NoError(t, db.DB.First(&updatedSell, sellOrder.ID).Error)
	assert.True(t, updatedSell.IsDone)
	assert.Equal(t, 0, *updatedSell.RemainingParts)

	// ✅ BUY order – delimično izvršen
	var updatedBuy types.Order
	assert.NoError(t, db.DB.First(&updatedBuy, buyOrder.ID).Error)
	assert.False(t, updatedBuy.IsDone)
	assert.Equal(t, 2, *updatedBuy.RemainingParts) // još 2 čeka

	// ✅ Kupcu dodat portfolio +3
	var buyPortfolio types.Portfolio
	err = db.DB.Where("user_id = ? AND security_id = ?", buyOrder.UserID, buyOrder.SecurityID).
		First(&buyPortfolio).Error
	assert.NoError(t, err)
	assert.Equal(t, 3, int(buyPortfolio.Quantity))

	// ✅ Portfolio SELL korisnika bi trebao da bude obrisan
	var sellPortfolio types.Portfolio
	err = db.DB.Where("user_id = ? AND security_id = ?", sellOrder.UserID, sellOrder.SecurityID).
		First(&sellPortfolio).Error
	assert.Error(t, err)
	assert.Equal(t, gorm.ErrRecordNotFound, err)

	// ✅ Security volumen smanjen za 3
	var updatedSecurity types.Security
	assert.NoError(t, db.DB.First(&updatedSecurity, security.ID).Error)
	assert.Equal(t, int64(997), updatedSecurity.Volume)

	fmt.Println("✅ Test partial fill bez AON uspešan")
}

func TestMatchOrderExecution_SequentialSell_Matching(t *testing.T) {
	err := db.InitTestDatabase()
	assert.NoError(t, err)

	err1 := os.Setenv("BANKING_SERVICE", "http://localhost:8082")
	if err1 != nil {
		return
	}

	// 🔹 Listing i security
	listing := types.Listing{
		ID:     12,
		Ticker: "GOOG",
		Type:   "Stock",
		Ask:    2800.0,
		Bid:    2750.0,
		Price:  2780.0,
	}
	assert.NoError(t, db.DB.Create(&listing).Error)

	security := types.Security{
		ID:     12,
		Ticker: "GOOG",
		Name:   "Google",
		Volume: 1000,
	}
	assert.NoError(t, db.DB.Create(&security).Error)

	// 🔹 SELL korisnik #1 – 3 hartije
	assert.NoError(t, db.DB.Create(&types.Portfolio{
		UserID:     41,
		SecurityID: 12,
		Quantity:   3,
	}).Error)

	// 🔹 SELL korisnik #2 – 3 hartije
	assert.NoError(t, db.DB.Create(&types.Portfolio{
		UserID:     42,
		SecurityID: 12,
		Quantity:   3,
	}).Error)

	// 🔹 SELL order #1
	sell1 := types.Order{
		ID:             6001,
		UserID:         41,
		AccountID:      701,
		SecurityID:     12,
		Quantity:       3,
		RemainingParts: ptr(3),
		ContractSize:   1,
		Direction:      "sell",
		Status:         "approved",
		AON:            false,
		Margin:         false,
	}
	assert.NoError(t, db.DB.Create(&sell1).Error)

	// 🔹 SELL order #2
	sell2 := types.Order{
		ID:             6002,
		UserID:         42,
		AccountID:      702,
		SecurityID:     12,
		Quantity:       3,
		RemainingParts: ptr(3),
		ContractSize:   1,
		Direction:      "sell",
		Status:         "approved",
		AON:            false,
		Margin:         false,
	}
	assert.NoError(t, db.DB.Create(&sell2).Error)

	// 🔹 BUY order za 5
	buy := types.Order{
		ID:             6003,
		UserID:         43,
		AccountID:      703,
		SecurityID:     12,
		Quantity:       5,
		RemainingParts: ptr(5),
		ContractSize:   1,
		Direction:      "buy",
		Status:         "approved",
		AON:            false,
		Margin:         false,
	}
	assert.NoError(t, db.DB.Create(&buy).Error)

	// 🔹 Matchuj BUY
	orders.MatchOrder(buy)

	//time.Sleep(5 * time.Second) // ⏳ čekamo obe iteracije

	for i := 0; i < 10; i++ {
		var updated types.Order
		_ = db.DB.First(&updated, buy.ID).Error
		if updated.RemainingParts != nil && *updated.RemainingParts == 0 {
			break
		}
		time.Sleep(500 * time.Millisecond)
	}

	// ✅ BUY order treba da bude završen
	var updatedBuy types.Order
	assert.NoError(t, db.DB.First(&updatedBuy, buy.ID).Error)
	assert.True(t, updatedBuy.IsDone)
	assert.Equal(t, 0, *updatedBuy.RemainingParts)

	// ✅ SELL #1 je završen
	var updatedSell1 types.Order
	assert.NoError(t, db.DB.First(&updatedSell1, sell1.ID).Error)
	assert.True(t, updatedSell1.IsDone)
	assert.Equal(t, 0, *updatedSell1.RemainingParts)

	// ✅ SELL #2 ima još 1 hartiju da proda
	var updatedSell2 types.Order
	assert.NoError(t, db.DB.First(&updatedSell2, sell2.ID).Error)
	assert.False(t, updatedSell2.IsDone)
	assert.Equal(t, 1, *updatedSell2.RemainingParts)

	// ✅ Portfolio BUY korisnika: +5
	var buyPortfolio types.Portfolio
	assert.NoError(t, db.DB.Where("user_id = ? AND security_id = ?", buy.UserID, buy.SecurityID).First(&buyPortfolio).Error)
	assert.Equal(t, 5, int(buyPortfolio.Quantity))

	// ✅ Portfolio SELL #1: ne postoji više (prodao sve)
	var s1Portfolio types.Portfolio
	err = db.DB.Where("user_id = ? AND security_id = ?", sell1.UserID, sell1.SecurityID).First(&s1Portfolio).Error
	assert.Equal(t, gorm.ErrRecordNotFound, err)

	// ✅ Portfolio SELL #2: ostala još 1 hartija
	var s2Portfolio types.Portfolio
	assert.NoError(t, db.DB.Where("user_id = ? AND security_id = ?", sell2.UserID, sell2.SecurityID).First(&s2Portfolio).Error)
	assert.Equal(t, 1, int(s2Portfolio.Quantity))

	// ✅ Security volume smanjen za 5
	var updatedSec types.Security
	assert.NoError(t, db.DB.First(&updatedSec, security.ID).Error)
	assert.Equal(t, int64(995), updatedSec.Volume)

	fmt.Println("✅ Test za više SELL ordera prošao")
}

func ptr(i int) *int {
	return &i
}
