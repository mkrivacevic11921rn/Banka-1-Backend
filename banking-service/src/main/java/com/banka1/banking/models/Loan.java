package com.banka1.banking.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double nominalRate;

    @Column(nullable = false)
    private Double effectiveRate;

    @Column(nullable = false)
    private Double loanAmount;

    @Column(nullable = false)
    private Integer duration; // in months

    @Column(nullable = false)
    private Long createdDate;

    @Column(nullable = false)
    private Long allowedDate;

    @Column(nullable = false)
    private Double monthlyPayment;

    @Column(nullable = false)
    private Long nextPaymentDate;

    @Column(nullable = false)
    private Double remainingAmount;

    @JoinColumn(name = "account_id", nullable = false)
    @ManyToOne
    private Account account;
}
