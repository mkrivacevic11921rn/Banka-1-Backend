package com.banka1.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExchangePairDTO {
    private String baseCurrency;  // Osnovna valuta (EUR, USD, RSD...)
    private String targetCurrency; // Ciljana valuta
    private Double exchangeRate; // Kurs
    private LocalDate date; // Datum kursa
}
