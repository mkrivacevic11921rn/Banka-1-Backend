package com.banka1.banking.models;

import com.banka1.banking.models.helper.TransferStatus;
import com.banka1.banking.models.helper.TransferType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Transfer {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_account_id", nullable = false)
    private Account fromAccountId;

    @ManyToOne
    @JoinColumn(name = "to_account_id", nullable = false)
    private Account toAccountId;

    @Column(nullable = false)
    private Double amount;

    @JoinColumn(nullable = true)
    @ManyToOne
    private Receiver receiver;

    @Column(nullable = false)
    private String paymentCode; // sifra placanja

    @Column(nullable = false)
    private String paymentReference; // poziv na broj

    @Column(nullable = false)
    private String paymentDescription; // svrha placanja

    @ManyToOne
    @JoinColumn(name = "from_currency_id", nullable = false)
    private Currency fromCurrency;

    @ManyToOne
    @JoinColumn(name = "to_currency_id", nullable = false)
    private Currency toCurrency;

    @Column(nullable = false)
    private Long createdAt;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransferType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    @Column(nullable = true)
    private Long completedAt;

    @Column(nullable = true)
    private String note;
}
