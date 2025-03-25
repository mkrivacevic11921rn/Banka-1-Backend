package com.banka1.banking.services;

import com.banka1.banking.models.Account;
import com.banka1.banking.models.Currency;
import com.banka1.banking.models.Installment;
import com.banka1.banking.models.Loan;
import com.banka1.banking.models.Transaction;
import com.banka1.banking.models.Transfer;
import com.banka1.banking.models.helper.CurrencyType;
import com.banka1.banking.models.helper.TransferStatus;
import com.banka1.banking.models.helper.TransferType;
import com.banka1.banking.repository.AccountRepository;
import com.banka1.banking.repository.CurrencyRepository;
import com.banka1.banking.repository.TransactionRepository;
import com.banka1.banking.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private BankAccountUtils bankAccountUtils;

    @InjectMocks
    private TransactionService transactionService;

    private Account fromAccount;
    private Account toAccount;
    private Transfer internalTransfer;
    private Transfer externalTransfer;
    private Currency currency;

    @BeforeEach
    void setUp() {
        // Setup test accounts
        fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setBalance(1000.0);
        fromAccount.setCurrencyType(CurrencyType.USD);


        toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setBalance(500.0);
        toAccount.setCurrencyType(CurrencyType.USD);

        // Setup currency
        currency = new Currency();
        currency.setCode(CurrencyType.USD);

        // Setup internal transfer
        internalTransfer = new Transfer();
        internalTransfer.setId(1L);
        internalTransfer.setFromAccountId(fromAccount);
        internalTransfer.setToAccountId(toAccount);
        internalTransfer.setAmount(100.0);
        internalTransfer.setStatus(TransferStatus.PENDING);
        internalTransfer.setType(TransferType.INTERNAL);
        internalTransfer.setFromCurrency(currency);
        internalTransfer.setToCurrency(currency);

        // Setup external transfer
        externalTransfer = new Transfer();
        externalTransfer.setId(2L);
        externalTransfer.setFromAccountId(fromAccount);
        externalTransfer.setToAccountId(toAccount);
        externalTransfer.setAmount(100.0);
        externalTransfer.setStatus(TransferStatus.PENDING);
        externalTransfer.setType(TransferType.EXTERNAL);
        externalTransfer.setFromCurrency(currency);
        externalTransfer.setToCurrency(currency);
    }

    @Test
    void testGetTransactionsByUserId() {
        Long userId = 1L;
        List<Account> accounts = Arrays.asList(fromAccount);

        // Kreiramo 2 transakcije sa ISTIM transferId-em (simulacija debit i credit transakcije)
        Transaction tx1 = new Transaction();
        tx1.setTransfer(internalTransfer);

        Transaction tx2 = new Transaction();
        tx2.setTransfer(internalTransfer); // isti transfer!

        List<Transaction> mockedTransactions = Arrays.asList(tx1, tx2);

        when(bankAccountUtils.getBankAccountForCurrency(any())).thenReturn(new Account());

        when(accountRepository.findByOwnerID(userId)).thenReturn(accounts);
        when(transactionRepository.findByFromAccountIdInOrToAccountIdIn(accounts, accounts))
                .thenReturn(mockedTransactions);

        List<Transaction> actualTransactions = transactionService.getTransactionsByUserId(userId);

        // Očekujemo da backend vrati SAMO 1 transakciju jer su obe bile za isti transfer
        assertEquals(1, actualTransactions.size());
    }


    @Test
    void testCalculateInstallment_WithInterest() {
        Double loanAmount = 1000.0;
        Double annualInterestRate = 12.0; // 12%
        Integer numberOfInstallments = 12;

        Double installment = transactionService.calculateInstallment(loanAmount, annualInterestRate, numberOfInstallments);

        // Expected monthly payment with 1% monthly interest rate
        // Using formula: P * [r(1+r)^n]/[(1+r)^n-1]
        assertTrue(installment > 83.0 && installment < 89.0); // Approximate value should be around 88.85
    }

    @Test
    void testCalculateInstallment_ZeroInterest() {
        Double loanAmount = 1000.0;
        Double annualInterestRate = 0.0;
        Integer numberOfInstallments = 10;

        Double installment = transactionService.calculateInstallment(loanAmount, annualInterestRate, numberOfInstallments);

        assertEquals(100.0, installment);
    }

    @Test
    void testProcessInstallment_Success() {
        Account customerAccount = new Account();
        customerAccount.setBalance(1000.0);
        customerAccount.setCurrencyType(CurrencyType.USD);

        Account bankAccount = new Account();
        bankAccount.setBalance(5000.0);

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setLoanAmount(1000.0);
        loan.setNumberOfInstallments(10);

        Installment installment = new Installment();
        installment.setLoan(loan);
        installment.setInterestRate(12.0);

        Currency currency = new Currency();
        currency.setCode(CurrencyType.USD);

        when(currencyRepository.getByCode(customerAccount.getCurrencyType())).thenReturn(currency);

        Boolean result = transactionService.processInstallment(customerAccount, bankAccount, installment);

        assertTrue(result);
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void testProcessInstallment_InsufficientFunds() {
        Account customerAccount = new Account();
        customerAccount.setBalance(50.0);
        customerAccount.setCurrencyType(CurrencyType.USD);

        Account bankAccount = new Account();
        bankAccount.setBalance(5000.0);

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setLoanAmount(1000.0);
        loan.setNumberOfInstallments(10);

        Installment installment = new Installment();
        installment.setLoan(loan);
        installment.setInterestRate(12.0);

        Boolean result = transactionService.processInstallment(customerAccount, bankAccount, installment);

        assertFalse(result);
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }
}