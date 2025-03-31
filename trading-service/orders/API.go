package orders

import (
	"banka1.com/middlewares"
	"strings"

	"banka1.com/db"
	"banka1.com/types"
	"github.com/go-playground/validator/v10"
	"github.com/gofiber/fiber/v2"
)

var validate = validator.New(validator.WithRequiredStructEnabled())

func OrderToOrderResponse(order types.Order) types.OrderResponse {
	return types.OrderResponse{
		ID:                order.ID,
		UserID:            order.UserID,
		AccountID:         order.AccountID,
		SecurityID:        order.SecurityID,
		Quantity:          order.Quantity,
		ContractSize:      order.ContractSize,
		StopPricePerUnit:  order.StopPricePerUnit,
		LimitPricePerUnit: order.LimitPricePerUnit,
		Direction:         order.Direction,
		Status:            order.Status,
		ApprovedBy:        order.ApprovedBy,
		IsDone:            order.IsDone,
		LastModified:      order.LastModified,
		RemainingParts:    order.RemainingParts,
		AfterHours:        order.AfterHours,
		AON:               order.AON,
		Margin:            order.Margin,
	}
}

// GetOrderByID godoc
//
//	@Summary		Preuzimanje naloga po I
//	@Summary		Preuzimanje naloga po ID-u
//	@Description	Vraća detalje specifičnog naloga na osnovu njegovog jedinstvenog identifikatora (ID).
//	@Tags			Orders
//	@Produce		json
//	@Param			id	path		int											true	"ID naloga koji se preuzima"
//	@Success		200	{object}	types.Response{data=types.OrderResponse}	"Uspešno preuzet nalog"
//	@Failure		400	{object}	types.Response								"Nevalidan ID naloga"
//	@Failure		404	{object}	types.Response								"Nalog sa datim ID-jem ne postoji"
//	@Router			/orders/{id} [get]
func GetOrderByID(c *fiber.Ctx) error {
	id, err := c.ParamsInt("id", -1)
	if err != nil || id <= 0 {
		var response types.Response
		response.Success = false
		if err != nil {
			response.Error = "Nevalidan ID: " + err.Error()
		} else {
			response.Error = "Nevalidan ID"
		}
		return c.Status(400).JSON(response)
	}
	var order types.Order
	if err := db.DB.First(&order, id).Error; err != nil {
		return c.Status(404).JSON(types.Response{
			Success: false,
			Error:   "Nije pronadjen: " + err.Error(),
		})
	}
	return c.JSON(types.Response{
		Success: true,
		Data:    OrderToOrderResponse(order),
	})
}

// GetOrders godoc
//
//	@Summary		Preuzimanje liste naloga
//	@Description	Vraća listu naloga, opciono filtriranu po statusu.
//	@Tags			Orders
//	@Produce		json
//	@Param			filter_status	query		string										false	"Status naloga za filtriranje. Podrazumevano 'all' za sve statuse."	default(all)	example(pending)
//	@Success		200				{object}	types.Response{data=[]types.OrderResponse}	"Uspešno preuzeta lista naloga"
//	@Failure		500				{object}	types.Response								"Greška pri preuzimanju naloga iz baze"
//	@Router			/orders [get]
func GetOrders(c *fiber.Ctx) error {
	filterStatus := strings.ToLower(c.Query("filter_status", "all"))
	var orders []types.Order
	var err error
	if "all" == filterStatus {
		err = db.DB.Find(&orders).Error
	} else {
		err = db.DB.Find(&orders, "status = ?", filterStatus).Error
	}
	if err != nil {
		return c.Status(400).JSON(types.Response{
			Success: false,
			Error:   "Neuspela pretraga: " + err.Error(),
		})
	}
	responses := make([]types.OrderResponse, len(orders))
	for i, order := range orders {
		responses[i] = OrderToOrderResponse(order)
	}
	return c.JSON(types.Response{
		Success: true,
		Data:    responses,
	})
}

//	@Descripti
//
// CreateOrder godoc
//
//	@Summary		Kreiranje novog naloga
//	@Description	Kreira novi nalog za hartije od vrednosti.
//	@Tags			Orders
//	@Accept			json
//	@Produce		json
//	@Param			orderRequest	body	types.CreateOrderRequest	true	"Podaci neophodni za kreiranje naloga"
//	@Security		BearerAuth
//	@Success		201	{object}	types.Response{data=uint}	"Uspešno kreiran nalog, vraća ID novog naloga"
//	@Failure		400	{object}	types.Response				"Neispravan format, neuspela validacija ili greška pri upisu u bazu"
//	@Failure		403	{object}	types.Response				"Nije dozvoljeno kreirati nalog za drugog korisnika"
//	@Router			/orders [post]
func CreateOrder(c *fiber.Ctx) error {
	var orderRequest types.CreateOrderRequest
	userId := c.Locals("user_id").(float64)

	if err := c.BodyParser(&orderRequest); err != nil {
		return c.Status(400).JSON(types.Response{
			Success: false,
			Error:   "Neuspelo parsiranje: " + err.Error(),
		})
	}
	if err := validate.Struct(orderRequest); err != nil {
		return c.Status(400).JSON(types.Response{
			Success: false,
			Error:   "Neuspela validacija: " + err.Error(),
		})
	}
	if userId != float64(orderRequest.UserID) {
		return c.Status(403).JSON(types.Response{
			Success: false,
			Error:   "Cannot create order for another user",
		})
	}

	order := types.Order{
		UserID:            orderRequest.UserID,
		AccountID:         orderRequest.AccountID,
		SecurityID:        orderRequest.SecurityID,
		Quantity:          orderRequest.Quantity,
		ContractSize:      orderRequest.ContractSize,
		StopPricePerUnit:  orderRequest.StopPricePerUnit,
		LimitPricePerUnit: orderRequest.LimitPricePerUnit,
		Direction:         orderRequest.Direction,
		Status:            "pending", // TODO: pribaviti needs approval vrednost preko token-a?
		ApprovedBy:        nil,
		IsDone:            false,
		RemainingParts:    &orderRequest.Quantity,
		AfterHours:        false, // TODO: dodati check za ovo
		AON:               orderRequest.AON,
		Margin:            orderRequest.Margin,
	}
	tx := db.DB.Create(&order)
	if err := tx.Error; err != nil {
		return c.Status(400).JSON(types.Response{
			Success: false,
			Error:   "Neuspelo kreiranje: " + err.Error(),
		})
	}
	return c.JSON(types.Response{
		Success: true,
		Data:    order.ID,
	})
}

func ApproveDeclineOrder(c *fiber.Ctx, decline bool) error {
	id, err := c.ParamsInt("id", -1)
	if err != nil || id <= 0 {
		var response types.Response
		response.Success = false
		if err != nil {
			response.Error = "Nevalidan ID: " + err.Error()
		} else {
			response.Error = "Nevalidan ID"
		}
		return c.Status(400).JSON(response)
	}
	var order types.Order
	if err := db.DB.First(&order, id).Error; err != nil {
		return c.Status(404).JSON(types.Response{
			Success: false,
			Error:   "Nije pronadjen: " + err.Error(),
		})
	}
	if order.Status != "pending" {
		return c.Status(400).JSON(types.Response{
			Success: false,
			Error:   "Nije na cekanju",
		})
	}
	if decline {
		order.Status = "declined"
	} else {
		// TODO: provera da li je vremenski ogranicen?
		order.Status = "approved"
	}
	order.ApprovedBy = new(uint)
	*order.ApprovedBy = 0 // TODO: dobavi iz token-a
	db.DB.Save(&order)
	return c.JSON(types.Response{
		Success: true,
		Data:    order.ID,
	})
}

// DeclineOrder godoc
//
//	@Summary		Odbijanje naloga
//	@Description	Menja status naloga u 'declined'.
//	@Tags			Orders
//	@Produce		json
//	@Param			id	path	int	true	"ID naloga koji se odbija"
//	@Security		BearerAuth
//	@Success		200	{object}	types.Response{data=uint}	"Nalog uspešno odbijen, vraća ID naloga"
//	@Failure		400	{object}	types.Response				"Nevalidan ID ili nalog nije u 'pending' statusu"
//	@Failure		403	{object}	types.Response				"Nedovoljne privilegije"
//	@Failure		404	{object}	types.Response				"Nalog sa datim ID-jem ne postoji"
//	@Failure		500	{object}	types.Response				"Interna Greška Servera"
//	@Router			/orders/{id}/decline [post]
func DeclineOrder(c *fiber.Ctx) error {
	return ApproveDeclineOrder(c, true)
}

// ApproveOrder godoc
//
//	@Summary		Odobravanje naloga
//	@Description	Menja status naloga u 'approved'.
//	@Tags			Orders
//	@Produce		json
//	@Param			id	path	int	true	"ID naloga koji se odobrava"
//	@Security		BearerAuth
//	@Success		200	{object}	types.Response{data=uint}	"Nalog uspešno odobren"
//	@Failure		400	{object}	types.Response				"Nevalidan ID ili nalog nije u 'pending' statusu"
//	@Failure		403	{object}	types.Response				"Nedovoljne privilegije"
//	@Failure		404	{object}	types.Response				"Nalog sa datim ID-jem ne postoji"
//	@Failure		500	{object}	types.Response				"Interna Greška Servera"
//	@Router			/orders/{id}/approve [post]
func ApproveOrder(c *fiber.Ctx) error {
	return ApproveDeclineOrder(c, false)
}

func InitRoutes(app *fiber.App) {

	app.Get("/orders/:id", GetOrderByID)
	app.Get("/orders", GetOrders)
	app.Post("/orders", middlewares.Auth, CreateOrder)
	app.Post("/orders/:id/decline", middlewares.Auth, middlewares.DepartmentCheck("SUPERVISOR"), DeclineOrder)
	app.Post("/orders/:id/approve", middlewares.Auth, middlewares.DepartmentCheck("SUPERVISOR"), ApproveOrder)
}
