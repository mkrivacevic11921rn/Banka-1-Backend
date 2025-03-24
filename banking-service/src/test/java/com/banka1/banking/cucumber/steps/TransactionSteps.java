package com.banka1.banking.cucumber.steps;

import com.banka1.banking.models.*;
import com.banka1.banking.models.helper.*;
import com.banka1.banking.repository.*;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration
public class TransactionSteps {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private Account fromAccount;
    private Account toAccount;
    private Transfer transfer;
    private ResponseEntity<Map> response;

    @Given("korisnik sa ID-em {int} ima naloge i izvršen interni transfer između svojih naloga")
    public void korisnik_sa_id_em_ima_naloge_i_izvrsen_interni_transfer(Integer userId) {
        Currency rsdCurrency = currencyRepository.findByCode(CurrencyType.RSD).orElseGet(() -> {
            Currency c = new Currency();
            c.setCode(CurrencyType.RSD);
            c.setName("Serbian Dinar");
            c.setCountry("Serbia");
            c.setSymbol("RSD");
            return currencyRepository.save(c);
        });

        fromAccount = new Account();
        fromAccount.setOwnerID(userId.longValue());
        fromAccount.setAccountNumber("123456");
        fromAccount.setBalance(500.0);
        fromAccount.setCurrencyType(CurrencyType.RSD);
        fromAccount.setStatus(AccountStatus.ACTIVE);
        fromAccount = accountRepository.save(fromAccount);

        toAccount = new Account();
        toAccount.setOwnerID(userId.longValue()); // oba naloga su korisnikova
        toAccount.setAccountNumber("654321");
        toAccount.setBalance(100.0);
        toAccount.setCurrencyType(CurrencyType.RSD);
        toAccount.setStatus(AccountStatus.ACTIVE);
        toAccount = accountRepository.save(toAccount);

        transfer = new Transfer();
        transfer.setFromAccountId(fromAccount);
        transfer.setToAccountId(toAccount);
        transfer.setAmount(50.0);
        transfer.setType(TransferType.INTERNAL);
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setFromCurrency(rsdCurrency);
        transfer.setToCurrency(rsdCurrency);
        transfer = transferRepository.save(transfer);

        // Kreiramo 2 transakcije kao u servisu
        Transaction debit = new Transaction();
        debit.setFromAccountId(fromAccount);
        debit.setToAccountId(toAccount);
        debit.setAmount(50.0);
        debit.setCurrency(rsdCurrency);
        debit.setTransfer(transfer);
        debit.setTimestamp(System.currentTimeMillis());
        transactionRepository.save(debit);

        Transaction credit = new Transaction();
        credit.setFromAccountId(fromAccount);
        credit.setToAccountId(toAccount);
        credit.setAmount(50.0);
        credit.setCurrency(rsdCurrency);
        credit.setTransfer(transfer);
        credit.setTimestamp(System.currentTimeMillis());
        transactionRepository.save(credit);
    }

    @When("pozove endpoint GET /transactions/{int}")
    public void pozove_endpoint_get_transactions(Integer userId) {
        String url = "http://localhost:8082/transactions/" + userId;
        response = restTemplate.getForEntity(url, Map.class);
    }

    @Then("dobija listu sa {int} transakcijom za taj transfer")
    public void dobija_listu_sa_transakcijom(Integer expectedCount) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map data = (Map) response.getBody().get("data");
        assertNotNull(data);

        var transactions = (Iterable<?>) data.get("data");
        assertEquals(expectedCount, ((Collection<?>) transactions).size());
    }
}
