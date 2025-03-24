package com.banka1.banking.services;

import com.banka1.banking.models.Account;
import com.banka1.banking.models.Installment;
import com.banka1.banking.models.Transaction;
import com.banka1.banking.models.helper.CurrencyType;
import com.banka1.banking.repository.AccountRepository;
import com.banka1.banking.repository.CurrencyRepository;
import com.banka1.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CurrencyRepository currencyRepository;
    private final BankAccountUtils bankAccountUtils;

    @Transactional
    public List<Transaction> getTransactionsByUserId(Long userId) {
        List<Account> accounts = accountRepository.findByOwnerID(userId);
        List<Transaction> transactions = transactionRepository.findByFromAccountIdIn(accounts);
        if(!Objects.equals(bankAccountUtils.getBankAccountForCurrency(CurrencyType.RSD).getOwnerID(), userId))
            transactions.removeIf(Transaction::getBankOnly);
        return transactions;
    }

    public Double calculateInstallment(Double loanAmount, Double annualInterestRate, Integer numberOfInstallments) {
        Double monthlyInterestRate = annualInterestRate / 12 / 100;  // Kamatna stopa kao decimalni broj

        // Ako je mesečna kamatna stopa 0 (kredit bez kamate)
        if (monthlyInterestRate == 0) {
            return loanAmount / numberOfInstallments; // Ako nema kamate, rata je samo podeljeni iznos kredita
        }

        // Izračunavanje mesečne rate koristeći formulu
        Double numerator = monthlyInterestRate * Math.pow(1 + monthlyInterestRate, numberOfInstallments);
        Double denominator = Math.pow(1 + monthlyInterestRate, numberOfInstallments) - 1;
        Double installmentAmount = loanAmount * (numerator / denominator);

        return installmentAmount;
    }

    public Boolean processInstallment(Account customerAccount, Account bankAccount, Installment installment) {
        Double loanAmount = installment.getLoan().getLoanAmount(); // Iznos kredita
        Double annualInterestRate = installment.getInterestRate(); // Godišnja kamatna stopa
        Integer numberOfInstallments = installment.getLoan().getNumberOfInstallments(); // Broj rata

        Double amount = calculateInstallment(loanAmount, annualInterestRate, numberOfInstallments);

            if (customerAccount.getBalance().compareTo(amount) >= 0) {
                customerAccount.setBalance(customerAccount.getBalance() - amount);
                bankAccount.setBalance(bankAccount.getBalance() + amount);
                Transaction transaction = new Transaction();
                transaction.setFromAccountId(customerAccount);
                transaction.setToAccountId(bankAccount);
                transaction.setFinalAmount(amount);
                transaction.setAmount(amount);
                transaction.setFee(0.0);
                transaction.setCurrency(currencyRepository.getByCode(customerAccount.getCurrencyType()));
                transaction.setTimestamp(Instant.now().getEpochSecond());
                transaction.setDescription("Installment for loan " + installment.getLoan());
                transaction.setLoanId(installment.getLoan().getId());

                transactionRepository.save(transaction);
                accountRepository.save(customerAccount);
                accountRepository.save(bankAccount);
                return true;
            }
        return false;
    }
}