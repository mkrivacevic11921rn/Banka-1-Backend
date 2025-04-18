package utils

import (
	"bytes"
	"encoding/json"
	"io"
)

func StructToJsonReader(data interface{}) io.Reader {
	jsonBytes, _ := json.Marshal(data)
	return bytes.NewReader(jsonBytes)
}
