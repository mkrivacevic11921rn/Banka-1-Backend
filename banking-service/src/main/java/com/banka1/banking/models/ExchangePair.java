package com.banka1.banking.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ExchangePair {

    @Id
    private Long id;

    @JoinColumn(nullable = false)
    @ManyToOne
    private Currency baseCurrency;

    @JoinColumn(nullable = false)
    @ManyToOne
    private Currency targetCurrency;

    @Column(nullable = false)
    private Double exchangeRate;
}
