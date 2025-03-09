package com.banka1.banking.models.helper;

public enum PaymentStatus {
    PENDING, //na cekanju
    APPROVED, // odobren
    DENIED, //odbijen
    PAID_OFF, //otplacen
    LATE // u kasnjenju
}
