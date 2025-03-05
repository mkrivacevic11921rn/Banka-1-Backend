package com.banka1.banking.bootstrap;

import com.banka1.banking.models.*;
import com.banka1.banking.models.helper.*;
import com.banka1.banking.repository.*;
import com.banka1.banking.services.CurrencyService;
import com.banka1.banking.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BootstrapData implements CommandLineRunner {

    private final CurrencyRepository currencyRepository;
    private final CurrencyService currencyService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final ReceiverRepository receiverRepository;
    @Autowired
    public BootstrapData(CurrencyRepository currencyRepository, CurrencyService currencyService, AccountRepository accountRepository, TransactionRepository transactionRepository, TransferRepository transferRepository, ReceiverRepository receiverRepository) {
        this.currencyRepository = currencyRepository;
        this.currencyService = currencyService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
        this.receiverRepository = receiverRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("============== Loading Data ==============");

        Account account = new Account();
        account.setAccountNumber("1234567890123456");
        account.setBalance(1000.0);
        account.setCompany(null);
        account.setDailyLimit(10000.0);
        account.setMonthlyLimit(100000.0);
        account.setDailySpent(0.0);
        account.setMonthlySpent(0.0);
        account.setCurrencyType(CurrencyType.RSD);
        account.setExpirationDate(1630454400000L);
        account.setCreatedDate(1627776000000L);
        account.setEmployeeID(1L);
        account.setMonthlyMaintenanceFee(0.0);
        account.setReservedBalance(0.0);
        account.setOwnerID(1L);
        account.setStatus(AccountStatus.ACTIVE);
        account.setType(AccountType.CURRENT);
        account.setSubtype(AccountSubtype.BUSINESS);
        account.setId(1L);

        accountRepository.save(account);

        Receiver receiver = new Receiver();
        receiver.setAddress("Nemanjina 11");
        receiver.setAccountNumber("1234567890123456");
        receiver.setOwnerAccountId(1L);
        receiver.setFirstName("John");
        receiver.setLastName("Doe");

        receiverRepository.save(receiver);

        if (currencyRepository.count() == 0) {
            List<Currency> currencies = new ArrayList<>();

            Currency rsd = new Currency();
            rsd.setCode(CurrencyType.RSD);
            rsd.setName("Serbian Dinar");
            rsd.setCountry("Serbia");
            rsd.setSymbol("дин.");
            currencies.add(rsd);

            Currency eur = new Currency();
            eur.setCode(CurrencyType.EUR);
            eur.setName("Euro");
            eur.setCountry("European Union");
            eur.setSymbol("€");
            currencies.add(eur);

            Currency usd = new Currency();
            usd.setCode(CurrencyType.USD);
            usd.setName("US Dollar");
            usd.setCountry("United States");
            usd.setSymbol("$");
            currencies.add(usd);

            Currency gbp = new Currency();
            gbp.setCode(CurrencyType.GBP);
            gbp.setName("British Pound");
            gbp.setCountry("United Kingdom");
            gbp.setSymbol("£");
            currencies.add(gbp);

            Currency chf = new Currency();
            chf.setCode(CurrencyType.CHF);
            chf.setName("Swiss Franc");
            chf.setCountry("Switzerland");
            chf.setSymbol("Fr");
            currencies.add(chf);

            Currency jpy = new Currency();
            jpy.setCode(CurrencyType.JPY);
            jpy.setName("Japanese Yen");
            jpy.setCountry("Japan");
            jpy.setSymbol("¥");
            currencies.add(jpy);

            Currency cad = new Currency();
            cad.setCode(CurrencyType.CAD);
            cad.setName("Canadian Dollar");
            cad.setCountry("Canada");
            cad.setSymbol("C$");
            currencies.add(cad);

            Currency aud = new Currency();
            aud.setCode(CurrencyType.AUD);
            aud.setName("Australian Dollar");
            aud.setCountry("Australia");
            aud.setSymbol("A$");
            currencies.add(aud);

            // Čuvanje u bazi
            currencyRepository.saveAll(currencies);
            currencyService.fetchExchangeRates();
            System.out.println("Currencies have been initialized in the database.");

            Transfer transfer = new Transfer();
            transfer.setAmount(100.0);
            transfer.setFromCurrency(rsd);
            transfer.setToCurrency(rsd);
            transfer.setStatus(TransferStatus.COMPLETED);
            transfer.setFromAccountId(account);
            transfer.setToAccountId(account);
            transfer.setCompletedAt(1627776000000L);
            transfer.setCreatedAt(1627776000000L);
            transfer.setOtp("123456");
            transfer.setPaymentCode("123456");
            transfer.setAdress("Nemanjina 11");
            transfer.setPaymentReference("123456");
            transfer.setType(TransferType.INTERNAL);
            transfer.setPaymentDescription("Initial deposit");
            transfer.setReceiver("John Doe");

            transferRepository.save(transfer);

            Transaction transaction = new Transaction();
            transaction.setAmount(100.0);
            transaction.setCurrency(rsd);
            transaction.setDescription("Initial deposit");
            transaction.setTimestamp(1627776000000L);
            transaction.setFromAccountId(account);
            transaction.setToAccountId(account);
            transaction.setTransfer(transfer);
            transaction.setId(1L);

            transactionRepository.save(transaction);
        } else {
            System.out.println("Currencies are already present in the database.");
        }

        System.out.println("============== Data Loaded ==============");
    }
}
