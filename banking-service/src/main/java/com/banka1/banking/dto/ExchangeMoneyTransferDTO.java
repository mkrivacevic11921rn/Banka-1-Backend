package com.banka1.banking.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ExchangeMoneyTransferDTO {

    private Long accountFrom;
    private Long accountTo;
    private Double amount;

}
