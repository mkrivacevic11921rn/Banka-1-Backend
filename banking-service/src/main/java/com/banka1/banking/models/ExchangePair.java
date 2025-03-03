package com.banka1.banking.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class ExchangePair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(nullable = false)
    @ManyToOne
    private Currency baseCurrency;

    @JoinColumn(nullable = false)
    @ManyToOne
    private Currency targetCurrency;

    @Column(nullable = false)
    private Double exchangeRate;

    @Column(nullable = false)
    private LocalDate date;
}
