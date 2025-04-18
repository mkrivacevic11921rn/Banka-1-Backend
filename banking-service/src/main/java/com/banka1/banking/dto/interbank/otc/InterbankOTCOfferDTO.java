package com.banka1.banking.dto.interbank.otc;

import lombok.Data;

@Data
public class InterbankOTCOfferDTO {
    private String stock;
    private int quantity;
    private double pricePerUnit;
    private double premium;
    private String settlementDate;
    private ForeignBankUser buyer;
    private ForeignBankUser seller;
}