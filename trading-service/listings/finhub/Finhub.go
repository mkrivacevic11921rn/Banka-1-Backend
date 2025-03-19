package finhub

import (
	"context"
	"os"

	finnhub "github.com/Finnhub-Stock-API/finnhub-go/v2"
)

func GetConfig() *finnhub.Configuration {
	cfg := finnhub.NewConfiguration()
	cfg.AddDefaultHeader("X-Finnhub-Token", os.Getenv("FINNHUB_KEY"))
	return cfg
}

func contains(slice []string, item string) bool {
	for _, v := range slice {
		if v == item {
			return true
		}
	}
	return false
}

func GetAllStock() (map[string]string, error) {
	finnhubClient := finnhub.NewAPIClient(GetConfig()).DefaultApi

	res, _, err := finnhubClient.StockSymbols(context.Background()).Exchange("US").Execute()
	if err != nil {
		return nil, err
	}

	stocksData := map[string]string{}

	for _, stock := range res {
		// if not COmmom Stock skip
		if *stock.Type != "Common Stock" {
			continue
		}
		stocksData[*stock.DisplaySymbol] = *stock.Mic
		break
	}

	return stocksData, nil
}

func GetStockData(ticker string) (finnhub.Quote, error) {
	finnhubClient := finnhub.NewAPIClient(GetConfig()).DefaultApi
	res, _, err := finnhubClient.Quote(context.Background()).Symbol(ticker).Execute()
	if err != nil {
		return finnhub.Quote{}, err
	}

	return res, nil
}
