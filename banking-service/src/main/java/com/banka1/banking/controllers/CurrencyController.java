package com.banka1.banking.controllers;

import com.banka1.banking.dto.ExchangePairDTO;
import com.banka1.banking.models.helper.CurrencyType;
import com.banka1.banking.services.CurrencyService;
import com.banka1.banking.utils.ResponseTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/currency")
public class CurrencyController {
    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableCurrencies() {
        List<String> currencies = currencyService.getAvailableCurrencies();
        return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("currencies", currencies), null);
    }

    @GetMapping("/exchange-rates")
    public ResponseEntity<?> getAllExchangeRates() {
        List<ExchangePairDTO> rates = currencyService.getAllExchangeRates();
        return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("rates", rates), null);
    }

    @GetMapping("/exchange-rates/{currency}")
    public ResponseEntity<?> getExchangeRatesForCurrency(@PathVariable CurrencyType currency) {
        List<ExchangePairDTO> rates = currencyService.getExchangeRatesForBaseCurrency(currency);
        if (rates.isEmpty()) {
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.NOT_FOUND), false, null,
                    "No exchange rates available for base currency: " + currency);
        }
        return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("rates", rates), null);
    }

//    @PostMapping("/fetch-rates")
//    public ResponseEntity<?> triggerFetchExchangeRates() {
//        try {
//            currencyService.fetchExchangeRates();
//            return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("message", "Exchange rates fetched successfully"), null);
//        } catch (Exception e) {
//            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR), false, null,
//                    "Failed to fetch exchange rates: " + e.getMessage());
//        }
//    }
}
