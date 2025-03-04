package com.banka1.banking.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String otpCode;

    @Column(nullable = false)
    private Long transferId;

    @Column(nullable = false)
    private Long expirationTime;

    @Column(nullable = false)
    private boolean used = false;


}
