package com.banka1.banking.dto.interbank.otc;

import lombok.Data;

@Data
public class ForeignBankUser {
    private int routingNumber;
    private String userId;
}