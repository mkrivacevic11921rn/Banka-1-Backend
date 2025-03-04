package com.banka1.banking.dto.response;

import com.banka1.banking.models.Company;
import com.banka1.banking.models.helper.AccountType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class AccountResponse {
    @NonNull
    private Long id;
    @NonNull
    private Long ownerID;
    @NonNull
    private Long employeeID;
    @NonNull
    private AccountType accountType;
    @NonNull
    private Company company;
}
