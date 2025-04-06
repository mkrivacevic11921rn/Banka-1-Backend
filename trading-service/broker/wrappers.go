package broker

func GetCustomerById(id int64) (*CustomerResponse, error) {
	var m CustomerResponse
	err := sendAndRecieve("get-customer", id, &m)
	if err != nil {
		return nil, err
	}
	return &m, nil
}
