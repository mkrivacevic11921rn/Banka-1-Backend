package com.banka1.banking.services;

import com.banka1.banking.dto.ExchangePairDTO;
import com.banka1.banking.models.Currency;
import com.banka1.banking.models.ExchangePair;
import com.banka1.banking.models.helper.CurrencyType;
import com.banka1.banking.repository.CurrencyRepository;
import com.banka1.banking.repository.ExchangePairRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CurrencyService {
    private final ExchangePairRepository exchangePairRepository;
    private final RestTemplate restTemplate;
    private final CurrencyRepository currencyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/RSD";
    private static final List<CurrencyType> SUPPORTED_CURRENCIES = Arrays.asList(CurrencyType.values());

    @Autowired
    public CurrencyService(ExchangePairRepository exchangePairRepository, RestTemplate restTemplate, CurrencyRepository currencyRepository) {
        this.exchangePairRepository = exchangePairRepository;
        this.restTemplate = restTemplate;
        this.currencyRepository = currencyRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Pokrece se svaku dan u 00:00
    public void fetchExchangeRates() {
        Map<String, Double> rates = getExchangeRatesFromApi();

        if (rates != null && !rates.isEmpty()) {
            for (CurrencyType currencyType : SUPPORTED_CURRENCIES) {
                String currencyCode = currencyType.name(); // Dobijamo "EUR", "USD" itd.

                if (rates.containsKey(currencyCode)) {
                    // Dohvatanje valute iz baze
                    Currency baseCurrency = currencyRepository.findByCode(CurrencyType.RSD)
                            .orElseThrow(() -> new RuntimeException("Base currency RSD not found in database"));

                    Currency targetCurrency = currencyRepository.findByCode(currencyType)
                            .orElseThrow(() -> new RuntimeException("Target currency " + currencyCode + " not found in database"));

                    // Kreiranje kursnog para
                    ExchangePair exchangePair = new ExchangePair();
                    exchangePair.setBaseCurrency(baseCurrency);
                    exchangePair.setTargetCurrency(targetCurrency);
                    exchangePair.setExchangeRate(rates.get(currencyCode));
                    exchangePair.setDate(LocalDate.now());

                    // Čuvanje u bazi
                    exchangePairRepository.save(exchangePair);
                }
            }
        } else {
            System.out.println("No exchange rates available from API.");
        }
    }

    private Map<String, Double> getExchangeRatesFromApi() {
        try {
            String jsonResponse = restTemplate.getForObject(API_URL, String.class);
            if (jsonResponse != null) {
                JsonNode root = objectMapper.readTree(jsonResponse);
                JsonNode ratesNode = root.get("rates");
                return objectMapper.convertValue(ratesNode, new TypeReference<Map<String, Double>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public List<ExchangePairDTO> getAllExchangeRates() {
        return exchangePairRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ExchangePairDTO> getExchangeRatesForBaseCurrency(CurrencyType baseCurrency) {
        // Ako tražimo kurs u odnosu na RSD, direktno vraćamo podatke iz baze
        if (baseCurrency.equals(CurrencyType.RSD)) {
            return exchangePairRepository.findByBaseCurrencyCode(baseCurrency).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        // Dohvatimo sve RSD → Ostale valute parove iz baze
        List<ExchangePair> rsdRates = exchangePairRepository.findByBaseCurrencyCode(CurrencyType.RSD);

        // Pronađemo koliko RSD vredi u toj bazičnoj valuti (EUR, USD...)
        Optional<ExchangePair> baseCurrencyRateOpt = rsdRates.stream()
                .filter(pair -> pair.getTargetCurrency().getCode().equals(baseCurrency))
                .findFirst();

        // Ako kurs za traženu valutu ne postoji, bacamo grešku
        if (baseCurrencyRateOpt.isEmpty()) {
            throw new RuntimeException("Exchange rate for base currency " + baseCurrency + " not found");
        }

        double baseCurrencyRate = baseCurrencyRateOpt.get().getExchangeRate();

        // Sada koristimo taj kurs da izračunamo sve ostale parove
        return rsdRates.stream()
                .filter(pair -> !pair.getTargetCurrency().getCode().equals(baseCurrency)) // Isključujemo baznu valutu
                .map(pair -> new ExchangePairDTO(
                        baseCurrency.name(),
                        pair.getTargetCurrency().getCode().name(),
                        pair.getExchangeRate() / baseCurrencyRate, // Formula
                        pair.getDate()
                ))
                .collect(Collectors.toList());
    }

    private ExchangePairDTO mapToDTO(ExchangePair pair) {
        return new ExchangePairDTO(
                pair.getBaseCurrency().getCode().name(),
                pair.getTargetCurrency().getCode().name(),
                pair.getExchangeRate(),
                pair.getDate()
        );
    }

    public List<String> getAvailableCurrencies() {
        return currencyRepository.findAll()
                .stream()
                .map(currency -> currency.getCode().name()) // Pretvara CurrencyType.EUR u "EUR"
                .collect(Collectors.toList());
    }
}
