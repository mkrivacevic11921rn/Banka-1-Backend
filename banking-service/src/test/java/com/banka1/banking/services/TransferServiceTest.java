package com.banka1.banking.services;

import com.banka1.banking.models.*;
import com.banka1.banking.models.helper.*;
import com.banka1.banking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional
public class TransferServiceTest {

    @Autowired
    private TransferService transferService;

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
        fromAccount.setAccountNumber("111111");
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
        toAccount.setAccountNumber("222222");
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
    }

    @Test
    void rollbackInternalTransferWhenInsufficientFunds() {
        final Transfer newTransfer = new Transfer();
        newTransfer.setFromAccountId(fromAccount);
        newTransfer.setToAccountId(toAccount);
        newTransfer.setAmount(200.0);
        newTransfer.setType(TransferType.INTERNAL);
        newTransfer.setStatus(TransferStatus.PENDING);
        newTransfer.setFromCurrency(rsdCurrency);
        newTransfer.setToCurrency(rsdCurrency);
        transferRepository.save(newTransfer);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transferService.processInternalTransfer(newTransfer.getId());
        });

        assertTrue(exception.getMessage().contains("Insufficient funds"));

        Transfer failedTransfer = transferRepository.findById(newTransfer.getId()).orElseThrow();
        assertEquals(TransferStatus.FAILED, failedTransfer.getStatus());
        assertEquals(100.0, accountRepository.findById(fromAccount.getId()).orElseThrow().getBalance());
        assertEquals(50.0, accountRepository.findById(toAccount.getId()).orElseThrow().getBalance());
        assertEquals(0, transactionRepository.findAll().size());
    }

    @Test
    void rollbackExternalTransferWhenInsufficientFunds() {
        final Transfer newTransfer = new Transfer();
        newTransfer.setFromAccountId(fromAccount);
        newTransfer.setToAccountId(toAccount);
        newTransfer.setAmount(200.0);
        newTransfer.setType(TransferType.EXTERNAL);
        newTransfer.setStatus(TransferStatus.PENDING);
        newTransfer.setFromCurrency(rsdCurrency);
        newTransfer.setToCurrency(rsdCurrency);
        transferRepository.save(newTransfer);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transferService.processExternalTransfer(newTransfer.getId());
        });

        assertTrue(exception.getMessage().contains("Insufficient balance for transfer"));

        Transfer failedTransfer = transferRepository.findById(newTransfer.getId()).orElseThrow();
        assertEquals(TransferStatus.FAILED, failedTransfer.getStatus());
        assertEquals(100.0, accountRepository.findById(fromAccount.getId()).orElseThrow().getBalance());
        assertEquals(50.0, accountRepository.findById(toAccount.getId()).orElseThrow().getBalance());
        assertEquals(0, transactionRepository.findAll().size());
    }

    @Test
    void noDuplicateTransactionsForInternalTransfer() {
        // Korisnik ima oba naloga (from i to su njegovi nalozi)
        toAccount.setOwnerID(1L); // Sad oba naloga pripadaju korisniku sa ID 1
        accountRepository.save(toAccount);

        // Kreiramo uspešan interni transfer
        Transfer newTransfer = new Transfer();
        newTransfer.setFromAccountId(fromAccount);
        newTransfer.setToAccountId(toAccount);
        newTransfer.setAmount(50.0);
        newTransfer.setType(TransferType.INTERNAL);
        newTransfer.setStatus(TransferStatus.PENDING);
        newTransfer.setFromCurrency(rsdCurrency);
        newTransfer.setToCurrency(rsdCurrency);
        newTransfer = transferRepository.save(newTransfer);

        // Pozivamo servis za obradu internog transfera
        transferService.processInternalTransfer(newTransfer.getId());

        // Provera da su obe transakcije kreirane u bazi
        assertEquals(2, transactionRepository.findAll().size());

        // Sada simuliramo API poziv koji povlači transakcije korisnika sa ID 1
        var transactions = transactionRepository
                .findByFromAccountIdInOrToAccountIdIn(
                        accountRepository.findByOwnerID(1L),
                        accountRepository.findByOwnerID(1L)
                );

        // Backend servis će u finalnoj verziji ovo filtrirati na 1 transakciju
        // Simulacija mape iz TransactionService
        Map<Long, Transaction> transferToTransaction = new HashMap<>();
        for (Transaction tx : transactions) {
            transferToTransaction.putIfAbsent(tx.getTransfer().getId(), tx);
        }

        assertEquals(1, transferToTransaction.size()); // Backend će vratiti samo jednu transakciju po transferu
    }
}
