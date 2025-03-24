package com.banka1.banking.services;

import com.banka1.banking.models.Account;
import com.banka1.banking.models.Currency;
import com.banka1.banking.models.Transfer;
import com.banka1.banking.models.helper.AccountStatus;
import com.banka1.banking.models.helper.AccountSubtype;
import com.banka1.banking.models.helper.AccountType;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional
class TransactionServiceRollbackTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Account fromAccount;
    private Account toAccount;
    private Transfer transfer;
    private Currency rsdCurrency;

    @BeforeEach
    void setUp() {
        rsdCurrency = currencyRepository.findByCode(CurrencyType.RSD).orElseGet(() -> {
            Currency c = new Currency();
            c.setCode(CurrencyType.RSD);
            c.setName("Serbian Dinar");
            c.setCountry("Serbia");
            c.setSymbol("RSD");
            return currencyRepository.save(c);
        });


        fromAccount = new Account();
        fromAccount.setOwnerID(1L);
        fromAccount.setAccountNumber("123456789");
        fromAccount.setBalance(100.0);
        fromAccount.setReservedBalance(0.0);
        fromAccount.setType(AccountType.CURRENT);
        fromAccount.setCurrencyType(CurrencyType.RSD);
        fromAccount.setSubtype(AccountSubtype.STANDARD);
        fromAccount.setCreatedDate(System.currentTimeMillis());
        fromAccount.setExpirationDate(System.currentTimeMillis() + 31536000000L);
        fromAccount.setDailyLimit(50000.0);
        fromAccount.setMonthlyLimit(1000000.0);
        fromAccount.setDailySpent(0.0);
        fromAccount.setMonthlySpent(0.0);
        fromAccount.setStatus(AccountStatus.ACTIVE);
        fromAccount.setEmployeeID(1L);
        fromAccount.setMonthlyMaintenanceFee(500.0);
        fromAccount = accountRepository.save(fromAccount);

        toAccount = new Account();
        toAccount.setOwnerID(2L);
        toAccount.setAccountNumber("987654321");
        toAccount.setBalance(50.0);
        toAccount.setReservedBalance(0.0);
        toAccount.setType(AccountType.CURRENT);
        toAccount.setCurrencyType(CurrencyType.RSD);
        toAccount.setSubtype(AccountSubtype.STANDARD);
        toAccount.setCreatedDate(System.currentTimeMillis());
        toAccount.setExpirationDate(System.currentTimeMillis() + 31536000000L);
        toAccount.setDailyLimit(50000.0);
        toAccount.setMonthlyLimit(1000000.0);
        toAccount.setDailySpent(0.0);
        toAccount.setMonthlySpent(0.0);
        toAccount.setStatus(AccountStatus.ACTIVE);
        toAccount.setEmployeeID(2L);
        toAccount.setMonthlyMaintenanceFee(500.0);
        toAccount = accountRepository.save(toAccount);

        transfer = new Transfer();
        transfer.setFromAccountId(fromAccount);
        transfer.setToAccountId(toAccount);
        transfer.setAmount(200.0);
        transfer.setType(TransferType.EXTERNAL);
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setFromCurrency(rsdCurrency);
        transfer.setToCurrency(rsdCurrency);
        transfer = transferRepository.save(transfer);
    }

    @Test
    void shouldRollbackTransactionOnFailure() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.processExternalTransfer(transfer.getId());
        });

        assertTrue(exception.getMessage().contains("Insufficient balance for transfer"));

        Transfer failedTransfer = transferRepository.findById(transfer.getId()).orElseThrow();
        assertEquals(TransferStatus.FAILED, failedTransfer.getStatus());

        Account updatedFromAccount = accountRepository.findById(fromAccount.getId()).orElseThrow();
        Account updatedToAccount = accountRepository.findById(toAccount.getId()).orElseThrow();

        assertEquals(100.0, updatedFromAccount.getBalance());
        assertEquals(50.0, updatedToAccount.getBalance());

        assertTrue(transactionRepository.findAll().isEmpty());
    }

    @Test
    void shouldProcessInternalTransferSuccessfully() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(fromAccount);
        transfer.setToAccountId(toAccount);
        transfer.setAmount(50.0);
        transfer.setType(TransferType.INTERNAL);
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setFromCurrency(rsdCurrency);
        transfer.setToCurrency(rsdCurrency);
        transfer = transferRepository.save(transfer);

        String result = transactionService.processInternalTransfer(transfer.getId());

        assertEquals("Transfer completed successfully", result);

        Transfer completedTransfer = transferRepository.findById(transfer.getId()).orElseThrow();
        assertEquals(TransferStatus.COMPLETED, completedTransfer.getStatus());

        Account updatedFromAccount = accountRepository.findById(fromAccount.getId()).orElseThrow();
        Account updatedToAccount = accountRepository.findById(toAccount.getId()).orElseThrow();

        assertEquals(50.0, updatedFromAccount.getBalance());
        assertEquals(100.0, updatedToAccount.getBalance());

        assertEquals(2, transactionRepository.findAll().size());
    }

    @Test
    void shouldRollbackInternalTransferOnFailure() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(fromAccount);
        transfer.setToAccountId(toAccount);
        transfer.setAmount(200.0);
        transfer.setType(TransferType.INTERNAL);
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setFromCurrency(rsdCurrency);
        transfer.setToCurrency(rsdCurrency);
        transfer = transferRepository.save(transfer);

        Long transferId = transfer.getId(); // ovo dodaj

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.processInternalTransfer(transferId);
        });


        assertTrue(exception.getMessage().contains("Insufficient funds"));

        Transfer failedTransfer = transferRepository.findById(transfer.getId()).orElseThrow();
        assertEquals(TransferStatus.FAILED, failedTransfer.getStatus());

        Account updatedFromAccount = accountRepository.findById(fromAccount.getId()).orElseThrow();
        Account updatedToAccount = accountRepository.findById(toAccount.getId()).orElseThrow();

        assertEquals(100.0, updatedFromAccount.getBalance());
        assertEquals(50.0, updatedToAccount.getBalance());

        assertTrue(transactionRepository.findAll().isEmpty());
    }

    @Test
    void shouldCalculateInstallmentCorrectly() {
        Double installment = transactionService.calculateInstallment(12000.0, 6.0, 12);
        assertNotNull(installment);
        assertTrue(installment > 0);
    }

}
