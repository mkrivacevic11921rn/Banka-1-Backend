package controllers

import (
	"banka1.com/db"
	"banka1.com/dto"
	"banka1.com/types"
	"github.com/go-playground/validator/v10"
	"github.com/gofiber/fiber/v2"
)

type ActuaryController struct {
}

func NewActuaryController() *ActuaryController {
	return &ActuaryController{}
}

var validate = validator.New()

func (ac *ActuaryController) CreateActuary(c *fiber.Ctx) error {
	var actuaryDTO dto.ActuaryDTO

	if err := c.BodyParser(&actuaryDTO); err != nil {
		response := types.Response{
			Success: false,
			Data:    nil,
			Error:   "Format zahteva nije ispravan",
		}
		return c.Status(400).JSON(response)
	}

	if err := validate.Struct(actuaryDTO); err != nil {
		response := types.Response{
			Success: false,
			Data:    nil,
			Error:   err.Error(),
		}
		return c.Status(400).JSON(response)
	}

	actuary := types.Actuary{
		UserID:       actuaryDTO.UserID,
		Role:         actuaryDTO.Role,
		LimitAmount:  actuaryDTO.LimitAmount,
		UsedLimit:    actuaryDTO.UsedLimit,
		NeedApproval: actuaryDTO.NeedApproval,
	}

	result := db.DB.Create(&actuary)
	if result.Error != nil {
		response := types.Response{
			Success: false,
			Data:    nil,
			Error:   result.Error.Error(),
		}
		return c.Status(500).JSON(response)
	}

	response := types.Response{
		Success: true,
		Data:    actuary,
		Error:   "",
	}

	return c.Status(201).JSON(response)
}

func (ac *ActuaryController) GetAllActuaries(c *fiber.Ctx) error {
	var actuaries []types.Actuary
	result := db.DB.Find(&actuaries)

	if result.Error != nil {
		response := types.Response{
			Success: false,
			Data:    nil,
			Error:   result.Error.Error(),
		}
		return c.Status(500).JSON(response)
	}

	response := types.Response{
		Success: true,
		Data:    result,
		Error:   "",
	}
	return c.JSON(response)
}

func (ac *ActuaryController) ChangeAgentLimits(c *fiber.Ctx) error {
	id := c.Params("ID")
	var actuary types.Actuary

	result := db.DB.First(&actuary, id)
	if result.Error != nil {
		return c.Status(404).JSON(types.Response{
			Success: false,
			Data:    nil,
			Error:   "Aktuar nije pronađen",
		})
	}

	var updateData dto.UpdateActuaryDTO
	if err := c.BodyParser(&updateData); err != nil {
		return c.Status(400).JSON(types.Response{
			Success: false,
			Data:    nil,
			Error:   "Neispravan format podataka",
		})
	}

	if updateData.LimitAmount != nil {
		actuary.LimitAmount = *updateData.LimitAmount
	}

	if updateData.ResetLimit {
		actuary.UsedLimit = 0
	}

	db.DB.Save(&actuary)

	return c.JSON(types.Response{
		Success: true,
		Data:    actuary,
		Error:   "",
	})
}

// POPRAVITI FILTER
func (ac *ActuaryController) FilterActuaries(c *fiber.Ctx) error {
	var actuaries []types.Actuary

	//name := c.Query("name")
	//surname := c.Query("surname")
	//email := c.Query("email")
	position := c.Query("position")

	query := db.DB

	//if name != "" {
	//	query = query.Joins("JOIN users ON users.id = actuaries.user_id").Where("users.name LIKE ?", "%"+name+"%")
	//}
	//if surname != "" {
	//	query = query.Joins("JOIN users ON users.id = actuaries.user_id").Where("users.surname LIKE ?", "%"+surname+"%")
	//}
	//if email != "" {
	//	query = query.Joins("JOIN users ON users.id = actuaries.user_id").Where("users.email LIKE ?", "%"+email+"%")
	//}
	if position != "" {
		query = query.Where("role = ?", position)
	}

	result := query.Find(&actuaries)

	if result.Error != nil {
		return c.Status(500).JSON(types.Response{
			Success: false,
			Data:    nil,
			Error:   result.Error.Error(),
		})
	}

	if len(actuaries) == 0 {
		return c.JSON(types.Response{
			Success: true,
			Data:    []types.Actuary{},
			Error:   "",
		})
	}

	return c.JSON(types.Response{
		Success: true,
		Data:    actuaries,
		Error:   "",
	})
}
