package com.banka1.banking.dto.request;

import com.banka1.banking.models.helper.AccountStatus;
import lombok.Data;

@Data
public class UpdateAccountDTO {
    private Double dailyLimit;
    private Double monthlyLimit;
    private AccountStatus status;
}
