package dto

type ActuaryDTO struct {
	UserID       uint    `json:"userID" validate:"required"`
	Role         string  `json:"role" validate:"required,oneof=supervisor agent"`
	LimitAmount  float64 `json:"limitAmount,omitempty"`
	UsedLimit    float64 `json:"usedLimit,omitempty"`
	NeedApproval bool    `json:"needApproval"`
}
