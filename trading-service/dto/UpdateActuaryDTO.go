package dto

type UpdateActuaryDTO struct {
	LimitAmount *float64 `json:"limitAmount,omitempty"`
	ResetLimit  bool     `json:"resetLimit"`
}
