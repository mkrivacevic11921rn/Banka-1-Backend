package com.banka1.banking.models;

import com.banka1.banking.models.helper.CurrencyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Currency {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyType code;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String symbol;
}
