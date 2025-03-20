package options

import "net/http"

type YahooOptionsApiResponse struct {
	OptionChain struct {
		Result []struct {
			ExpirationDate int64 `json:"expirationDate"`
			Options        []struct {
				Calls []struct {
					ContractSymbol    string  `json:"contractSymbol"`
					Strike            float64 `json:"strike"`
					LastPrice         float64 `json:"lastPrice"`
					Change            float64 `json:"change"`
					PercentChange     float64 `json:"percentChange"`
					Volume            int     `json:"volume"`
					OpenInterest      int     `json:"openInterest"`
					Bid               float64 `json:"bid"`
					Ask               float64 `json:"ask"`
					ImpliedVolatility float64 `json:"impliedVolatility"`
				} `json:"calls"`
				Puts []struct {
					ContractSymbol    string  `json:"contractSymbol"`
					Strike            float64 `json:"strike"`
					LastPrice         float64 `json:"lastPrice"`
					Change            float64 `json:"change"`
					PercentChange     float64 `json:"percentChange"`
					Volume            int     `json:"volume"`
					OpenInterest      int     `json:"openInterest"`
					Bid               float64 `json:"bid"`
					Ask               float64 `json:"ask"`
					ImpliedVolatility float64 `json:"impliedVolatility"`
				} `json:"puts"`
			} `json:"options"`
		} `json:"result"`
	} `json:"optionChain"`
}

func FetchYahoo(ticker string) error {
	// https://query1.finance.yahoo.com/v6/finance/options/AAPL

	client := &http.Client{}
	req, err := http.NewRequest("GET", "https://query1.finance.yahoo.com/v6/finance/options/"+ticker, nil)
	if err != nil {
		return err
	}

	req.Header.Set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
	req.Header.Set("Accept", "application/json")
	req.Header.Set("Referer", "https://finance.yahoo.com/quote/"+ticker)

	resp, err := client.Do(req)
	if err != nil {
		return err
	}

	_ = resp

	return nil
}
