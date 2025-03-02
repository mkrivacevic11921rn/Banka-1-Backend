package com.banka1.banking.dto.request;

import com.banka1.banking.models.helper.AccountStatus;
import com.banka1.banking.models.helper.AccountSubtype;
import com.banka1.banking.models.helper.AccountType;
import com.banka1.banking.models.helper.CurrencyType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class UpdateAccountDTO {
//    private Long ownerID;
//    private Double balance;
    private Double reservedBalance;
//    private AccountType type;
    private Long expirationDate;
    private Double dailyLimit;
    private Double monthlyLimit;
    private AccountSubtype subtype;
//    private Double dailySpent;
//    private Double monthlySpent;
    private AccountStatus status;
    private Double monthlyMaintenanceFee;
}
