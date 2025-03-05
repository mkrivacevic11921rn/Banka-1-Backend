package com.banka1.banking.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class InternalTransferDTO {

    private Long fromAccountId;
    private Long toAccountId;
    private Double amount;

}
