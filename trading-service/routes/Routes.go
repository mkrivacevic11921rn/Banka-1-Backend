package routes

import (
	"banka1.com/controllers"
	"github.com/gofiber/fiber/v2"
)

func Setup(app *fiber.App, actuaryController *controllers.ActuaryController) {

	//actuaryController := controllers.NewActuaryController()

	//Actuaries
	app.Post("/actuaries", actuaryController.CreateActuary)
	app.Get("/actuaries/all", actuaryController.GetAllActuaries)
	app.Put("actuaries/:ID", actuaryController.ChangeAgentLimits)
	app.Get("/actuaries/filter", actuaryController.FilterActuaries)
}
