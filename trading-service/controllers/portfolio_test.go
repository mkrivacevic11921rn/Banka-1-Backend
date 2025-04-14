package controllers

import (
	"banka1.com/db"
	"banka1.com/types"
	"bytes"
	"encoding/json"
	"github.com/gofiber/fiber/v2"
	"github.com/stretchr/testify/assert"
	"io"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

func TestInitPortfolioRoutes(t *testing.T) {
	// Setup
	app := fiber.New()

	// Execute
	InitPortfolioRoutes(app)

	// Verify routes are registered
	findRoute := func(method, path string) bool {
		for methodRoutes := range app.Stack() {
			for _, route := range app.Stack()[methodRoutes] {
				if route.Method == method && strings.Contains(route.Path, path) {
					return true
				}
			}
		}
		return false
	}

	// Check expected routes
	assert.True(t, findRoute("PUT", "/securities/public-count"))
	assert.True(t, findRoute("GET", "/portfolios"))
}

func TestUpdatePublicCountMissingUserID(t *testing.T) {
	// Setup
	app := fiber.New()
	controller := NewPortfolioController()
	app.Put("/securities/public-count", controller.UpdatePublicCount)

	// Create test request with valid body but missing user_id
	reqBody := `{"security_id": 1, "public": 5}`
	req := httptest.NewRequest(http.MethodPut, "/securities/public-count", bytes.NewBufferString(reqBody))
	req.Header.Set("Content-Type", "application/json")
	resp, _ := app.Test(req)

	// Verify
	assert.Equal(t, 401, resp.StatusCode)

	// Check response body
	body, _ := io.ReadAll(resp.Body)
	var response types.Response
	json.Unmarshal(body, &response)

	assert.False(t, response.Success)
	assert.Contains(t, response.Error, "Unauthorized")
}

func TestUpdatePublicCountInvalidRequestBody(t *testing.T) {
	// Setup
	app := fiber.New()
	controller := NewPortfolioController()
	app.Put("/securities/public-count", controller.UpdatePublicCount)

	// Create test request with invalid JSON
	reqBody := `{"security_id": "invalid", "public": true}`
	req := httptest.NewRequest(http.MethodPut, "/securities/public-count", bytes.NewBufferString(reqBody))
	req.Header.Set("Content-Type", "application/json")
	resp, _ := app.Test(req)

	// Verify
	assert.Equal(t, 400, resp.StatusCode)

	// Check response body
	body, _ := io.ReadAll(resp.Body)
	var response types.Response
	json.Unmarshal(body, &response)

	assert.False(t, response.Success)
	assert.Contains(t, response.Error, "Invalid request body")
}

func TestNewPortfolioController(t *testing.T) {
	// Verify that the constructor returns a non-nil controller
	controller := NewPortfolioController()
	assert.NotNil(t, controller)
}

func TestUpdatePublicCountTooHigh(t *testing.T) {
	app := fiber.New()
	app.Use(func(c *fiber.Ctx) error {
		// Simulacija autentifikovanog korisnika sa user_id = 123
		c.Locals("user_id", 123)
		return c.Next()
	})
	controller := NewPortfolioController()
	app.Put("/securities/public-count", controller.UpdatePublicCount)

	err := db.InitTestDatabase()
	assert.NoError(t, err)

	// Pripremi portfolio
	portfolio := types.Portfolio{
		UserID:      123,
		SecurityID:  1,
		Quantity:    5,
		PublicCount: 2,
	}
	err1 := db.DB.Create(&portfolio).Error
	assert.NoError(t, err1)

	// Pokušaj da postaviš više "public" nego što korisnik ima hartija
	reqBody := `{"security_id": 1, "public": 10}`
	req := httptest.NewRequest(http.MethodPut, "/securities/public-count", bytes.NewBufferString(reqBody))
	req.Header.Set("Content-Type", "application/json")

	resp, err := app.Test(req)
	assert.NoError(t, err)
	assert.Equal(t, 400, resp.StatusCode)

	body, _ := io.ReadAll(resp.Body)
	var response types.Response
	_ = json.Unmarshal(body, &response)

	assert.False(t, response.Success)
	assert.Contains(t, response.Error, "cannot be greater than total amount")
}

func TestUpdatePublicCountNegativeValue(t *testing.T) {
	app := fiber.New()
	controller := NewPortfolioController()
	app.Put("/securities/public-count", func(c *fiber.Ctx) error {
		// Simuliramo autentikaciju
		c.Locals("user_id", 99)
		return controller.UpdatePublicCount(c)
	})

	err := db.InitTestDatabase()
	assert.NoError(t, err)

	// Pripremi portfolio
	portfolio := types.Portfolio{
		UserID:     99,
		SecurityID: 500,
		Quantity:   5,
	}
	assert.NoError(t, db.DB.Create(&portfolio).Error)

	// Pokušaj da se postavi negativan public_count
	payload := map[string]interface{}{
		"security_id": 500,
		"public":      -3,
	}
	jsonBody, _ := json.Marshal(payload)

	req := httptest.NewRequest(http.MethodPut, "/securities/public-count", bytes.NewBuffer(jsonBody))
	req.Header.Set("Content-Type", "application/json")

	resp, err := app.Test(req)
	assert.NoError(t, err)
	assert.Equal(t, 400, resp.StatusCode)

	// Proveri odgovor
	body, _ := io.ReadAll(resp.Body)
	var response types.Response
	json.Unmarshal(body, &response)

	assert.False(t, response.Success)
	assert.Contains(t, response.Error, "cannot be negative")
}
