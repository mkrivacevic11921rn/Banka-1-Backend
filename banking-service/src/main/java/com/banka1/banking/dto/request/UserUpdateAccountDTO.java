package com.banka1.banking.dto.request;

import lombok.Data;

@Data
public class UserUpdateAccountDTO {
    private Double dailyLimit;
    private Double monthlyLimit;
}
