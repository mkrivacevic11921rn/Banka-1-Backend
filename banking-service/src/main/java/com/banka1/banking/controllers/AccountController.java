package com.banka1.banking.controllers;

import com.banka1.banking.dto.request.CreateAccountDTO;
import com.banka1.banking.dto.request.UpdateAccountDTO;
import com.banka1.banking.models.Account;
import com.banka1.banking.models.Transaction;
import com.banka1.banking.services.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Account API", description = "API za upravljanje racunima")
public class AccountController {
    @Autowired
    private AccountService accountService;

    /// pristup imaju samo zaposleni
    /// Da bi zaposleni mogao da kreira novi račun, potrebno je da se prijavi u aplikaciju.
    /// Pored čuvanja podataka o vlasniku (klijentu), čuvaju se i podaci o zaposlenima koji su napravili račune.
    /// Nakon prijave, izabira jedan od tipova računa: Tekući račun, Devizni račun. Nakon uspešnog kreiranog računa vlasnik dobija email o uspehu.
    @PostMapping("/")
    @Operation(summary = "Kreiranje računa",
            description = "Dodaje novi račun u sistem.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Račun uspešno kreiran.\n"),
            @ApiResponse(responseCode = "403", description = "Nevalidni podaci")
    })
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountDTO createAccountDTO) {
        Account savedAccount = null;
        try {
            savedAccount = accountService.createAccount(createAccountDTO);
        } catch (Exception e) {
            System.err.println(e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("broj racuna", savedAccount.getAccountNumber());
        data.put("message", "Račun uspešno kreiran.\n");
        response.put("data", data);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /// pristup imaju samo zaposleni
    @GetMapping("/")
    @Operation(summary = "Dohvatanje svih računa",
            description = "Vraća listu svih računa u sistemu.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista računa uspešno dohvaćena"),
            @ApiResponse(responseCode = "404", description = "Nema dostupnih računa")
    })
    public ResponseEntity<?> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();

        if (accounts == null || accounts.isEmpty()) {
            return ResponseEntity.ok("Ne postoje računi u sistemu.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", accounts);

        return ResponseEntity.ok(response);
    }

    /// pristup imaju zaposleni i vlasnici racuna
    @GetMapping("/user/{userId}")
    @Operation(summary = "Dohvatanje računa specificnog korisnika",
            description = "Vraća sve račune vezane za određenog korisnika.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Računi uspešno dohvaćeni"),
            @ApiResponse(responseCode = "404", description = "Nema računa za datog korisnika")
    })
    public ResponseEntity<?> getAccountsByOwner(@PathVariable Long userId) {
        List<Account> accounts = accountService.getAccountsByOwnerId(userId);

        if (accounts == null || accounts.isEmpty()) {
            return ResponseEntity.ok("Ovaj korisnik nema otvorenih računa.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", accounts);

        return ResponseEntity.ok(response);
    }

    /// pristup imaju samo zaposleni
    @PutMapping("/{accountId}")
    @Operation(summary = "Ažuriranje računa",
            description = "Omogućava zaposlenima da ažuriraju podatke o računu.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Račun uspešno ažuriran"),
            @ApiResponse(responseCode = "404", description = "Račun nije pronađen"),
            @ApiResponse(responseCode = "400", description = "Nevalidni podaci za ažuriranje")
    })
    public ResponseEntity<?> updateAccount(
            @PathVariable Long accountId,
            @RequestBody UpdateAccountDTO updateAccountDTO) {

        try {
            Account updatedAccount = accountService.updateAccount(accountId, updateAccountDTO);
            return ResponseEntity.ok(updatedAccount);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{accountId}/transactions")
    @Operation(summary = "Dohvatanje transakcija za izabrani račun",
            description = "Vraća sve transakcije za izabrani račun.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transakcije uspešno dohvaćene"),
            @ApiResponse(responseCode = "404", description = "Račun nije pronađen ili nema transakcija")
    })
    public ResponseEntity<?> getTransactionsForAccount(@PathVariable Long accountId) {
        List<Transaction> transactions = accountService.getTransactionsForAccount(accountId);

        if (transactions.isEmpty()) {
            return ResponseEntity.ok("Nisu pronadjene transakcije vezane za ovaj račun");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", transactions);

        return ResponseEntity.ok(response);
    }

}
