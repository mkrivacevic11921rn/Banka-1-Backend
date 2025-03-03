package com.banka1.banking.bootstrap;

import com.banka1.banking.models.Currency;
import com.banka1.banking.models.helper.CurrencyType;
import com.banka1.banking.repository.CurrencyRepository;
import com.banka1.banking.services.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BootstrapData implements CommandLineRunner {

    private final CurrencyRepository currencyRepository;
    private final CurrencyService currencyService;

    @Autowired
    public BootstrapData(CurrencyRepository currencyRepository, CurrencyService currencyService) {
        this.currencyRepository = currencyRepository;
        this.currencyService = currencyService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("============== Loading Data ==============");

        if (currencyRepository.count() == 0) {
            List<Currency> currencies = List.of(
                    new Currency(CurrencyType.RSD, "Serbian Dinar", "Serbia", "дин."),
                    new Currency(CurrencyType.EUR, "Euro", "European Union", "€"),
                    new Currency(CurrencyType.USD, "US Dollar", "United States", "$"),
                    new Currency(CurrencyType.GBP, "British Pound", "United Kingdom", "£"),
                    new Currency(CurrencyType.CHF, "Swiss Franc", "Switzerland", "Fr"),
                    new Currency(CurrencyType.JPY, "Japanese Yen", "Japan", "¥"),
                    new Currency(CurrencyType.CAD, "Canadian Dollar", "Canada", "C$"),
                    new Currency(CurrencyType.AUD, "Australian Dollar", "Australia", "A$")
            );

            currencyRepository.saveAll(currencies);
            currencyService.fetchExchangeRates();
            System.out.println("Currencies have been initialized in the database.");
        } else {
            System.out.println("Currencies are already present in the database.");
        }

        System.out.println("============== Data Loaded ==============");
    }
}
