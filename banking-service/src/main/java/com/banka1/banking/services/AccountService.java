package com.banka1.banking.services;

import com.banka1.banking.dto.response.AccountResponse;
import com.banka1.banking.models.Account;
import com.banka1.banking.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponse findById(String id) {
        return accountRepository.findById(Long.parseLong(id)).map(AccountService::getAccountResponse).orElse(null);
    }

    public static AccountResponse getAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getOwnerID(),
                account.getEmployeeID(),
                account.getType(),
                account.getCompany());
    }
}
