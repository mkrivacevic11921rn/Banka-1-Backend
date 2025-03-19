package main

import (
	"os"

	"banka1.com/db"
	"banka1.com/types"
	"github.com/gofiber/fiber/v2"
	"github.com/joho/godotenv"
	"banka1.com/orders"

	"log"
	"banka1.com/exchanges"
)

func main() {

	err := godotenv.Load()
	if err != nil {
		panic("Error loading .env file")
	}

	db.Init()

	err = exchanges.LoadDefaultExchanges()
    if err != nil {
        log.Printf("Warning: Failed to load exchanges: %v", err)
    }

	app := fiber.New()

	app.Get("/", func(c *fiber.Ctx) error {
		response := types.Response{
			Success: true,
			Data:    "Hello, World!",
			Error:   "",
		}
		return c.JSON(response)
	})

	app.Get("/exchanges", func(c *fiber.Ctx) error {
		/*
		Function to get all the exchanges from the database
		Model: 
			ID        uint   	UNIQUE
			Name      string 
			Acronym   string 
			MicCode   string 	UNIQUE
			Country   string 
			Currency  string 
			Timezone  string
			OpenTime  string 
			CloseTime string
		*/
        var exchanges []types.Exchange

        if result := db.DB.Find(&exchanges); result.Error != nil {
            return c.Status(500).JSON(types.Response{
                Success: false,
                Data:    nil,
                Error:   "Failed to fetch exchanges: " + result.Error.Error(),
            })
        }

        return c.JSON(types.Response{
            Success: true,
            Data:    exchanges,
            Error:   "",
        })
    })

	app.Get("/exchanges/:id", func(c *fiber.Ctx) error {
		/*
		Get exchange by ID.
		ID is a unique identifier for the exchange.
		ID is assigned in the code and is not in the exchamges.csv data.
		*/
		id := c.Params("id")

		var exchange types.Exchange

		if result := db.DB.First(&exchange, id); result.Error != nil {
			return c.Status(404).JSON(types.Response{
				Success: false,
				Data:    nil,
				Error:   "Exchange not found with ID: " + id,
			})
		}

		return c.JSON(types.Response{
			Success: true,
			Data:    exchange,
			Error:   "",
		})
	})

    app.Get("/exchanges/mic/:micCode", func(c *fiber.Ctx) error {
		/*
		Get exchange by MIC code.
		Mic code is a Unique identifier for the exchange.
		*/
        micCode := c.Params("micCode")
        
        var exchange types.Exchange
        
        if result := db.DB.Where("mic_code = ?", micCode).First(&exchange); result.Error != nil {
            return c.Status(404).JSON(types.Response{
                Success: false,
                Data:    nil,
                Error:   "Exchange not found with MIC code: " + micCode,
            })
        }

        return c.JSON(types.Response{
            Success: true,
            Data:    exchange,
            Error:   "",
        })
    })
    
    app.Get("/exchanges/acronym/:acronym", func(c *fiber.Ctx) error {
		/*
		Get exchange by acronym.
		Acronym is a short form of the exchange name.
		Acronym is usually unique BUT DOESNT HAVE TO BE!
		*/
        acronym := c.Params("acronym")
        
        var exchange types.Exchange
        
        if result := db.DB.Where("acronym = ?", acronym).First(&exchange); result.Error != nil {
            return c.Status(404).JSON(types.Response{
                Success: false,
                Data:    nil,
                Error:   "Exchange not found with acronym: " + acronym,
            })
        }
		
        return c.JSON(types.Response{
            Success: true,
            Data:    exchange,
            Error:   "",
        })
    })

	orders.InitRoutes(app)

	port := os.Getenv("PORT")
	app.Listen("localhost:" + port)
}
