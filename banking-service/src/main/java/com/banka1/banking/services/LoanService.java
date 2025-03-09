package com.banka1.banking.services;

import com.banka1.banking.dto.request.CreateLoanDTO;
import com.banka1.banking.listener.MessageHelper;
import com.banka1.banking.models.Account;
import com.banka1.banking.models.Loan;
import com.banka1.banking.models.helper.PaymentStatus;
import com.banka1.banking.repository.AccountRepository;
import com.banka1.banking.repository.LoanRepository;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final JmsTemplate jmsTemplate;
    private final MessageHelper messageHelper;
    private final ModelMapper modelMapper;
    private final String destinationEmail;
    private final AccountRepository accountRepository;

    public LoanService(LoanRepository loanRepository, JmsTemplate jmsTemplate, MessageHelper messageHelper, ModelMapper modelMapper, @Value("${destination.email}") String destinationEmail, AccountRepository accountRepository) {
        this.loanRepository = loanRepository;
        this.jmsTemplate = jmsTemplate;
        this.messageHelper = messageHelper;
        this.modelMapper = modelMapper;
        this.destinationEmail = destinationEmail;
        this.accountRepository = accountRepository;
    }

    public Loan createLoan(@Valid CreateLoanDTO createLoanDTO) {
        Account account = accountRepository.findById(createLoanDTO.getAccountId())
                .orElseThrow(() -> new RuntimeException("Racun nije pronadjen"));

        if (account == null) {return null;}

        Loan newLoan = modelMapper.map(createLoanDTO, Loan.class);
        newLoan.setPaymentStatus(PaymentStatus.PENDING);
        newLoan.setAccount(account);
        newLoan.setCreatedDate(Instant.now().getEpochSecond());

        newLoan = loanRepository.save(newLoan);
        return newLoan;
    }
}
