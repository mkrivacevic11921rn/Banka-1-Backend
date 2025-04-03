package routes

import (
	"banka1.com/middlewares"

	"github.com/gofiber/fiber/v2"
)

func Setup(app *fiber.App) {

	//actuaryController := controllers.NewActuaryController()

	// Sve rute vezane za aktuare su dostupne iskljucivo supervizorima

	app.Group("/actuaries", middlewares.Auth, middlewares.DepartmentCheck("supervisor"))

}
