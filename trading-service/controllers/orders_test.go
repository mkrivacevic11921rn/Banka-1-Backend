package controllers

import (
	"banka1.com/controllers/orders"
	"banka1.com/db"
	"banka1.com/types"
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/gofiber/fiber/v2"
	"github.com/stretchr/testify/assert"
	"net"
	"net/http"
	"net/http/httptest"
	"os"
	"testing"
	"time"
)

var app *fiber.App

func registerTestRoutes(app *fiber.App) {
	controller := NewOrderController()

	app.Get("/orders/:id", controller.GetOrderByID)
	app.Get("/orders", controller.GetOrders)

	// OVDE izbacujemo middlewares.Auth
	app.Post("/orders", controller.CreateOrder)
	app.Post("/orders/:id/decline", controller.DeclineOrder)
	app.Post("/orders/:id/approve", controller.ApproveOrder)
	app.Post("/orders/:id/cancel", controller.CancelOrder)
}

func TestMain(m *testing.M) {
	ln, err := net.Listen("tcp", ":0")
	if err != nil {
		panic("Cannot find free port: " + err.Error())
	}
	_, port, _ := net.SplitHostPort(ln.Addr().String())
	_ = os.Setenv("BANKING_SERVICE", "http://localhost:"+port)
	//_ = os.Setenv("BANKING_SERVICE", "http://"+addr)

	go func() {
		mockApp := fiber.New()
		mockApp.Post("/orders/execute/:token", func(c *fiber.Ctx) error {
			fmt.Println("MOCK /orders/execute HIT sa tokenom:", c.Params("token"))
			return c.SendStatus(200)
		})
		if err := mockApp.Listener(ln); err != nil {
			panic("Mock server nije mogao da se startuje: " + err.Error())
		}
	}()

	time.Sleep(300 * time.Millisecond)

	_ = db.InitTestDatabase()
	app = fiber.New()

	app.Use(func(c *fiber.Ctx) error {
		if userId := c.Get("X-Test-UserID"); userId != "" {
			c.Locals("user_id", 1.0)
		}
		if dept := c.Get("X-Test-Department"); dept != "" {
			c.Locals("department", dept)
		}
		return c.Next()
	})

	registerTestRoutes(app)

	code := m.Run()
	os.Exit(code)
}

func setupMockBankingRoutes() {
	app.Post("/orders/execute/:token", func(c *fiber.Ctx) error {
		fmt.Println("MOCK execute hit")
		return c.SendStatus(200)
	})
}

func createTestOrder(t *testing.T, approved bool) types.Order {
	security := types.Security{
		ID:        1,
		Ticker:    "MSFT",
		Name:      "Microsoft",
		Volume:    1000,
		LastPrice: 100.0,
	}
	_ = db.DB.Create(&security).Error

	order := types.Order{
		UserID:         1,
		AccountID:      1,
		SecurityID:     1,
		Quantity:       5,
		ContractSize:   1,
		Direction:      "buy",
		Status:         "pending",
		RemainingParts: ptr(5),
		OrderType:      "LIMIT",
	}
	if approved {
		order.Status = "approved"
		id := uint(99)
		order.ApprovedBy = &id
	}
	_ = db.DB.Create(&order).Error
	return order
}

func TestCreateOrder_Success(t *testing.T) {
	body := map[string]any{
		"user_id":       1,
		"account_id":    1,
		"security_id":   1,
		"quantity":      5,
		"contract_size": 1,
		"direction":     "buy",
	}
	_ = db.DB.Create(&types.Security{ID: 1, Ticker: "TSLA", Volume: 100, LastPrice: 50.0, Name: "Tesla"}).Error

	payload, _ := json.Marshal(body)
	req := httptest.NewRequest(http.MethodPost, "/orders", bytes.NewReader(payload))
	req.Header.Set("Authorization", "Bearer mock-token")
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("X-Test-UserID", "1")

	resp, err := app.Test(req)
	assert.NoError(t, err)
	assert.Equal(t, 200, resp.StatusCode)
}

func TestApproveOrderAndMatch_Success(t *testing.T) {
	order := createTestOrder(t, false)

	req := httptest.NewRequest(http.MethodPost, fmt.Sprintf("/orders/%d/approve", order.ID), nil)
	req.Header.Set("Authorization", "Bearer mock-token")
	req.Header.Set("X-Test-UserID", "1")
	req.Header.Set("X-Test-Department", "SUPERVISOR")

	resp, err := app.Test(req)
	assert.NoError(t, err)
	assert.Equal(t, 200, resp.StatusCode)
}

func TestCancelOrder_Success(t *testing.T) {
	order := createTestOrder(t, false)

	req := httptest.NewRequest(http.MethodPost, fmt.Sprintf("/orders/%d/cancel", order.ID), nil)
	req.Header.Set("Authorization", "Bearer mock-token")
	req.Header.Set("X-Test-UserID", "1")

	resp, err := app.Test(req)
	assert.NoError(t, err)
	assert.Equal(t, 200, resp.StatusCode)
}

func TestDeclineOrder_Success(t *testing.T) {
	order := createTestOrder(t, false)

	req := httptest.NewRequest(http.MethodPost, fmt.Sprintf("/orders/%d/decline", order.ID), nil)
	req.Header.Set("Authorization", "Bearer mock-token")
	req.Header.Set("X-Test-UserID", "1")
	req.Header.Set("X-Test-Department", "SUPERVISOR")

	resp, err := app.Test(req)
	assert.NoError(t, err)
	assert.Equal(t, 200, resp.StatusCode)
}

func TestGetOrderByID_Success(t *testing.T) {
	order := createTestOrder(t, false)

	req := httptest.NewRequest(http.MethodGet, fmt.Sprintf("/orders/%d", order.ID), nil)
	resp, err := app.Test(req)
	assert.NoError(t, err)
	assert.Equal(t, 200, resp.StatusCode)
}

func TestGetOrders_Filtered(t *testing.T) {
	req := httptest.NewRequest(http.MethodGet, "/orders?filter_status=pending", nil)
	resp, err := app.Test(req)
	assert.NoError(t, err)
	assert.Equal(t, 200, resp.StatusCode)
}

func TestMatchOrder_FullExecution(t *testing.T) {
	security := types.Security{ID: 2, Ticker: "AAPL", Volume: 10, LastPrice: 100.0, Name: "Apple"}
	_ = db.DB.Create(&security).Error

	listing := types.Listing{
		ID:     1,
		Ticker: "AAPL",
		Ask:    100.0,
		Bid:    98.0,
		Price:  99.0,
		Type:   "STOCK",
	}
	_ = db.DB.Create(&listing).Error

	portfolio := types.Portfolio{
		UserID:     2,
		SecurityID: 2,
		Quantity:   5,
	}
	_ = db.DB.Create(&portfolio).Error

	seller := types.Order{
		UserID:         2,
		AccountID:      2,
		SecurityID:     2,
		Quantity:       5,
		RemainingParts: ptr(5),
		ContractSize:   1,
		Direction:      "sell",
		Status:         "approved",
	}
	buyer := types.Order{
		UserID:         3,
		AccountID:      3,
		SecurityID:     2,
		Quantity:       5,
		RemainingParts: ptr(5),
		ContractSize:   1,
		Direction:      "buy",
		Status:         "approved",
	}
	_ = db.DB.Create(&seller).Error
	_ = db.DB.Create(&buyer).Error

	orders.MatchOrder(buyer)

	timeout := time.After(20 * time.Second) // produžen timeout
	ticker := time.NewTicker(500 * time.Millisecond)
	defer ticker.Stop()

	for {
		select {
		case <-timeout:
			t.Fatalf("Order nije završen u očekivanom vremenu")
		case <-ticker.C:
			var updatedBuyer types.Order
			_ = db.DB.First(&updatedBuyer, buyer.ID).Error

			if updatedBuyer.IsDone && updatedBuyer.RemainingParts != nil && *updatedBuyer.RemainingParts == 0 {
				var updatedSeller types.Order
				_ = db.DB.First(&updatedSeller, seller.ID).Error
				assert.True(t, updatedSeller.IsDone)

				// Proveri transakcije
				var count int64
				_ = db.DB.Model(&types.Transaction{}).Where("order_id = ?", buyer.ID).Count(&count)
				assert.Equal(t, int64(1), count)

				// Proveri da li se smanjio volume
				var finalSecurity types.Security
				_ = db.DB.First(&finalSecurity, 2).Error
				assert.Equal(t, int64(5), finalSecurity.Volume)

				return
			}
		}
	}
}

func ptr(i int) *int {
	return &i
}

func TestMatchOrder_TransactionCreated(t *testing.T) {
	security := types.Security{ID: 3, Ticker: "GOOG", Volume: 10, LastPrice: 100.0, Name: "Google"}
	_ = db.DB.Create(&security).Error
	listing := types.Listing{ID: 2, Ticker: "GOOG", Ask: 100.0, Bid: 98.0, Price: 99.0, Type: "STOCK"}
	_ = db.DB.Create(&listing).Error
	portfolio := types.Portfolio{UserID: 4, SecurityID: 3, Quantity: 5}
	_ = db.DB.Create(&portfolio).Error

	seller := types.Order{UserID: 4, AccountID: 4, SecurityID: 3, Quantity: 5, RemainingParts: ptr(5), ContractSize: 1, Direction: "sell", Status: "approved"}
	buyer := types.Order{UserID: 5, AccountID: 5, SecurityID: 3, Quantity: 5, RemainingParts: ptr(5), ContractSize: 1, Direction: "buy", Status: "approved"}
	_ = db.DB.Create(&seller).Error
	_ = db.DB.Create(&buyer).Error

	orders.MatchOrder(buyer)
	time.Sleep(3 * time.Second)

	var txs []types.Transaction
	db.DB.Find(&txs, "order_id = ?", buyer.ID)
	assert.NotEmpty(t, txs, "Očekivana je barem jedna transakcija za order")
}

func TestMatchOrder_FeeCalculated(t *testing.T) {
	order := types.Order{OrderType: "MARKET"}
	fee := orders.CalculateFee(order, 100.0)
	assert.Equal(t, 7.0, fee)

	order = types.Order{OrderType: "LIMIT"}
	fee = orders.CalculateFee(order, 100.0)
	assert.Equal(t, 12.0, fee)

	order = types.Order{OrderType: "STOP"}
	fee = orders.CalculateFee(order, 100.0)
	assert.Equal(t, 0.0, fee)
}

func TestMatchOrder_RollbackOnFailure(t *testing.T) {
	_ = os.Setenv("BANKING_SERVICE", "http://invalid-host") // Forsiramo fail
	security := types.Security{ID: 4, Ticker: "TSLA", Volume: 10, LastPrice: 100.0, Name: "Tesla"}
	_ = db.DB.Create(&security).Error
	listing := types.Listing{ID: 3, Ticker: "TSLA", Ask: 100.0, Bid: 98.0, Price: 99.0, Type: "STOCK"}
	_ = db.DB.Create(&listing).Error
	portfolio := types.Portfolio{UserID: 6, SecurityID: 4, Quantity: 5}
	_ = db.DB.Create(&portfolio).Error

	seller := types.Order{UserID: 6, AccountID: 6, SecurityID: 4, Quantity: 5, RemainingParts: ptr(5), ContractSize: 1, Direction: "sell", Status: "approved"}
	buyer := types.Order{UserID: 7, AccountID: 7, SecurityID: 4, Quantity: 5, RemainingParts: ptr(5), ContractSize: 1, Direction: "buy", Status: "approved"}
	_ = db.DB.Create(&seller).Error
	_ = db.DB.Create(&buyer).Error

	orders.MatchOrder(buyer)
	time.Sleep(3 * time.Second)

	var failedBuyer types.Order
	_ = db.DB.First(&failedBuyer, buyer.ID).Error
	assert.False(t, failedBuyer.IsDone, "Order ne sme biti označen kao završen nakon greške")
}

func TestMatchOrder_PortfolioChanges(t *testing.T) {
	security := types.Security{ID: 4, Ticker: "GOOG", Volume: 10, LastPrice: 100.0, Name: "Google"}
	_ = db.DB.Create(&security).Error
	listing := types.Listing{ID: 2, Ticker: "GOOG", Ask: 100.0, Bid: 98.0, Price: 99.0, Type: "STOCK"}
	_ = db.DB.Create(&listing).Error

	// Dodaj početni portfolio prodavcu
	_ = db.DB.Create(&types.Portfolio{
		UserID:     10,
		SecurityID: 4,
		Quantity:   3,
	}).Error

	seller := types.Order{
		UserID:         10,
		AccountID:      10,
		SecurityID:     4,
		Quantity:       3,
		RemainingParts: ptr(3),
		ContractSize:   1,
		Direction:      "sell",
		Status:         "approved",
	}
	buyer := types.Order{
		UserID:         20,
		AccountID:      20,
		SecurityID:     4,
		Quantity:       3,
		RemainingParts: ptr(3),
		ContractSize:   1,
		Direction:      "buy",
		Status:         "approved",
	}
	_ = db.DB.Create(&seller).Error
	_ = db.DB.Create(&buyer).Error

	orders.MatchOrder(buyer)

	timeout := time.After(10 * time.Second)
	ticker := time.NewTicker(500 * time.Millisecond)
	defer ticker.Stop()

	for {
		select {
		case <-timeout:
			t.Fatal("Nije izvršen match u zadatom vremenu")
		case <-ticker.C:
			var updatedBuyer types.Order
			_ = db.DB.First(&updatedBuyer, buyer.ID).Error
			if updatedBuyer.IsDone {
				var buyPortfolio types.Portfolio
				var sellPortfolio types.Portfolio
				_ = db.DB.Where("user_id = ? AND security_id = ?", 20, 4).First(&buyPortfolio).Error
				_ = db.DB.Where("user_id = ? AND security_id = ?", 10, 4).First(&sellPortfolio).Error

				assert.Equal(t, 3, buyPortfolio.Quantity)
				assert.Equal(t, 0, sellPortfolio.Quantity)
				return
			}
		}
	}
}

func TestMatchOrder_AONInsufficientMatch(t *testing.T) {
	security := types.Security{ID: 5, Ticker: "NFLX", Volume: 2, LastPrice: 100.0, Name: "Netflix"}
	_ = db.DB.Create(&security).Error
	listing := types.Listing{ID: 4, Ticker: "NFLX", Ask: 100.0, Bid: 98.0, Price: 99.0, Type: "STOCK"}
	_ = db.DB.Create(&listing).Error
	_ = db.DB.Create(&types.Portfolio{UserID: 30, SecurityID: 5, Quantity: 3}).Error

	seller := types.Order{UserID: 30, AccountID: 30, SecurityID: 5, Quantity: 3, RemainingParts: ptr(3), ContractSize: 1, Direction: "sell", Status: "approved"}
	buyer := types.Order{UserID: 31, AccountID: 31, SecurityID: 5, Quantity: 5, RemainingParts: ptr(5), ContractSize: 1, Direction: "buy", Status: "approved", AON: true}
	_ = db.DB.Create(&seller).Error
	_ = db.DB.Create(&buyer).Error

	orders.MatchOrder(buyer)
	time.Sleep(2 * time.Second)

	var updated types.Order
	_ = db.DB.First(&updated, buyer.ID).Error
	assert.False(t, updated.IsDone)
	assert.Equal(t, 5, *updated.RemainingParts)
}

func TestMatchOrder_MarginWithoutPermission(t *testing.T) {
	security := types.Security{ID: 7, Ticker: "AMD", Volume: 10, LastPrice: 100.0, Name: "AMD"}
	_ = db.DB.Create(&security).Error
	listing := types.Listing{ID: 6, Ticker: "AMD", Ask: 100.0, Bid: 98.0, Price: 99.0, Type: "STOCK"}
	_ = db.DB.Create(&listing).Error
	_ = db.DB.Create(&types.Portfolio{UserID: 50, SecurityID: 7, Quantity: 5}).Error

	seller := types.Order{UserID: 50, AccountID: 50, SecurityID: 7, Quantity: 5, RemainingParts: ptr(5), ContractSize: 1, Direction: "sell", Status: "approved"}
	buyer := types.Order{UserID: 51, AccountID: 51, SecurityID: 7, Quantity: 5, RemainingParts: ptr(5), ContractSize: 1, Direction: "buy", Status: "approved", Margin: true}
	_ = db.DB.Create(&seller).Error
	_ = db.DB.Create(&buyer).Error

	orders.MatchOrder(buyer)
	time.Sleep(2 * time.Second)

	var updated types.Order
	_ = db.DB.First(&updated, buyer.ID).Error
	assert.False(t, updated.IsDone)
}

func TestMatchOrder_SelfMatch(t *testing.T) {
	security := types.Security{ID: 9, Ticker: "ORCL", Volume: 10, LastPrice: 100.0, Name: "Oracle"}
	_ = db.DB.Create(&security).Error
	listing := types.Listing{ID: 8, Ticker: "ORCL", Ask: 100.0, Bid: 98.0, Price: 99.0, Type: "STOCK"}
	_ = db.DB.Create(&listing).Error
	_ = db.DB.Create(&types.Portfolio{UserID: 70, SecurityID: 9, Quantity: 5}).Error

	order1 := types.Order{UserID: 70, AccountID: 70, SecurityID: 9, Quantity: 5, RemainingParts: ptr(5), ContractSize: 1, Direction: "sell", Status: "approved"}
	order2 := types.Order{UserID: 70, AccountID: 70, SecurityID: 9, Quantity: 5, RemainingParts: ptr(5), ContractSize: 1, Direction: "buy", Status: "approved"}
	_ = db.DB.Create(&order1).Error
	_ = db.DB.Create(&order2).Error

	orders.MatchOrder(order2)
	time.Sleep(2 * time.Second)

	var updated types.Order
	_ = db.DB.First(&updated, order2.ID).Error
	assert.False(t, updated.IsDone)
}
