package com.banka1.banking.services;

import com.banka1.banking.dto.response.LoanResponse;
import com.banka1.banking.models.Loan;
import com.banka1.banking.repository.LoanRepository;
import org.springframework.stereotype.Service;

@Service
public class LoanService {
    private final LoanRepository loanRepository;

    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public LoanResponse findById(String id) {
        return loanRepository.findById(Long.parseLong(id)).map(LoanService::getLoanResponse).orElse(null);
    }

    public static LoanResponse getLoanResponse(Loan loan) {
        return new LoanResponse(
                AccountService.getAccountResponse(loan.getAccount()));
    }
}
