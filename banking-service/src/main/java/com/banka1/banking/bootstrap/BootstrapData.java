package com.banka1.banking.bootstrap;

import com.banka1.banking.models.Currency;
import com.banka1.banking.models.helper.CurrencyType;
import com.banka1.banking.repository.CurrencyRepository;
import com.banka1.banking.services.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
            List<Currency> currencies = new ArrayList<>();

            Currency rsd = new Currency();
            rsd.setCode(CurrencyType.RSD);
            rsd.setName("Serbian Dinar");
            rsd.setCountry("Serbia");
            rsd.setSymbol("дин.");
            currencies.add(rsd);

            Currency eur = new Currency();
            eur.setCode(CurrencyType.EUR);
            eur.setName("Euro");
            eur.setCountry("European Union");
            eur.setSymbol("€");
            currencies.add(eur);

            Currency usd = new Currency();
            usd.setCode(CurrencyType.USD);
            usd.setName("US Dollar");
            usd.setCountry("United States");
            usd.setSymbol("$");
            currencies.add(usd);

            Currency gbp = new Currency();
            gbp.setCode(CurrencyType.GBP);
            gbp.setName("British Pound");
            gbp.setCountry("United Kingdom");
            gbp.setSymbol("£");
            currencies.add(gbp);

            Currency chf = new Currency();
            chf.setCode(CurrencyType.CHF);
            chf.setName("Swiss Franc");
            chf.setCountry("Switzerland");
            chf.setSymbol("Fr");
            currencies.add(chf);

            Currency jpy = new Currency();
            jpy.setCode(CurrencyType.JPY);
            jpy.setName("Japanese Yen");
            jpy.setCountry("Japan");
            jpy.setSymbol("¥");
            currencies.add(jpy);

            Currency cad = new Currency();
            cad.setCode(CurrencyType.CAD);
            cad.setName("Canadian Dollar");
            cad.setCountry("Canada");
            cad.setSymbol("C$");
            currencies.add(cad);

            Currency aud = new Currency();
            aud.setCode(CurrencyType.AUD);
            aud.setName("Australian Dollar");
            aud.setCountry("Australia");
            aud.setSymbol("A$");
            currencies.add(aud);

            // Čuvanje u bazi
            currencyRepository.saveAll(currencies);
            currencyService.fetchExchangeRates();
            System.out.println("Currencies have been initialized in the database.");
        } else {
            System.out.println("Currencies are already present in the database.");
        }

        System.out.println("============== Data Loaded ==============");
    }
}
