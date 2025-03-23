package com.banka1.banking.controllers;

import com.banka1.banking.aspect.AccountAuthorization;
import com.banka1.banking.dto.request.CreateAccountDTO;
import com.banka1.banking.dto.request.UpdateAccountDTO;
import com.banka1.banking.dto.request.UserUpdateAccountDTO;
import com.banka1.banking.models.Account;
import com.banka1.banking.models.Transaction;
import com.banka1.banking.services.AccountService;
import com.banka1.banking.services.implementation.AuthService;
import com.banka1.banking.utils.ResponseTemplate;
import com.banka1.banking.utils.ResponseMessage;
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
    @Autowired
    private AuthService authService;

    /// pristup imaju samo zaposleni
    /// Da bi zaposleni mogao da kreira novi račun, potrebno je da se prijavi u aplikaciju.
    /// Pored čuvanja podataka o vlasniku (klijentu), čuvaju se i podaci o zaposlenima koji su napravili račune.
    /// Nakon prijave, izabira jedan od tipova računa: Tekući račun, Devizni račun. Nakon uspešnog kreiranog računa vlasnik dobija email o uspehu.
    @PostMapping("/")
    @Operation(summary = "Kreiranje računa",
            description = "Dodaje novi račun u sistem.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Račun uspešno kreiran.\n"),
            @ApiResponse(responseCode = "403", description = "Nevalidni podaci")
    })
    @AccountAuthorization(employeeOnlyOperation = true)
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountDTO createAccountDTO, @RequestHeader(value = "Authorization", required = false) String authorization) {
        Account savedAccount = null;
        try {
            savedAccount = accountService.createAccount(createAccountDTO, authService.parseToken(authService.getToken(authorization)).get("id", Long.class));
        } catch (RuntimeException e) {
//            log.error("Greška prilikom kreiranja racuna: ", e);
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.BAD_REQUEST), false, null, e.getMessage());
        }

        if (savedAccount == null) {
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.SEE_OTHER), false, null, ResponseMessage.USER_NOT_FOUND.getMessage());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("broj racuna", savedAccount.getAccountNumber());
        response.put("message", "Račun uspešno kreiran.\n");

        return ResponseTemplate.create(ResponseEntity.status(HttpStatus.CREATED), true, response, null);
    }

    /// pristup imaju samo zaposleni
    @GetMapping("/")
    @Operation(summary = "Dohvatanje svih računa",
            description = "Vraća listu svih računa u sistemu.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista računa uspešno dohvaćena"),
            @ApiResponse(responseCode = "404", description = "Nema dostupnih računa")
    })
    @AccountAuthorization
    public ResponseEntity<?> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();

        Map<String, Object> response = new HashMap<>();
        response.put("accounts", accounts);

        return ResponseTemplate.create(ResponseEntity.status(HttpStatus.OK), true, response, null);
    }

    /// pristup imaju zaposleni i vlasnici racuna
    @GetMapping("/user/{userId}")
    @Operation(summary = "Dohvatanje računa specificnog korisnika",
            description = "Vraća sve račune vezane za određenog korisnika.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Računi uspešno dohvaćeni"),
            @ApiResponse(responseCode = "404", description = "Korisnik ne postoji")
    })
    @AccountAuthorization
    public ResponseEntity<?> getAccountsByOwner(@PathVariable Long userId) {

        List<Account> accounts = accountService.getAccountsByOwnerId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("accounts", accounts);

        return ResponseTemplate.create(ResponseEntity.status(HttpStatus.OK), true, response, null);
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
    @AccountAuthorization(employeeOnlyOperation = true)
    public ResponseEntity<?> updateAccount(
            @PathVariable Long accountId,
            @RequestBody UpdateAccountDTO updateAccountDTO) {

        try {
            Account updatedAccount = accountService.updateAccount(accountId, updateAccountDTO);
            return ResponseTemplate.create(ResponseEntity.ok(), true,
                    Map.of( "message", ResponseMessage.UPDATED, "data", updatedAccount), null);
        } catch (RuntimeException e) {
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.BAD_REQUEST), false, null, e.getMessage());
        }
    }

    /// pristup imaju zaposleni i vlasnici racuna
    @PutMapping("/user/{userId}/{accountId}")
    @Operation(summary = "Ažuriranje računa od strane korisnika",
            description = "Omogućava korisnicima da ažuriraju podatke o računu.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Račun uspešno ažuriran"),
            @ApiResponse(responseCode = "404", description = "Račun nije pronađen"),
            @ApiResponse(responseCode = "400", description = "Nevalidni podaci za ažuriranje")
    })
    public ResponseEntity<?> updateUserAccount(
            @PathVariable Long userId,
            @PathVariable Long accountId,
            @RequestBody UserUpdateAccountDTO updateAccountDTO) {

        try {
            Account updatedAccount = accountService.userUpdateAccount(userId, accountId, updateAccountDTO);
            if (updatedAccount == null) {
                return ResponseTemplate.create(ResponseEntity.status(HttpStatus.BAD_REQUEST), false,
                        Map.of( "message", ResponseMessage.NOT_THE_OWNER), null);

            }
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.OK), true,
                    Map.of( "message", ResponseMessage.UPDATED, "data", updatedAccount), null);
        } catch (RuntimeException e) {
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.BAD_REQUEST), false, null, e.getMessage());
        }
    }

    @GetMapping("/{accountId}/transactions")
    @Operation(summary = "Dohvatanje transakcija za izabrani račun",
            description = "Vraća sve transakcije za izabrani račun.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transakcije uspešno dohvaćene"),
            @ApiResponse(responseCode = "404", description = "Račun nije pronađen")
    })
    @AccountAuthorization
    public ResponseEntity<?> getTransactionsForAccount(@PathVariable Long accountId) {

        // Proveri da li račun postoji
        if (accountService.findById(accountId) == null) {
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.NOT_FOUND), false, null, "Račun sa ID-jem " + accountId + " nije pronađen.");
        }

        List<Transaction> transactions = accountService.getTransactionsForAccount(accountId);
        Map<String, Object> response = new HashMap<>();
        response.put("transactions", transactions);
        return ResponseTemplate.create(ResponseEntity.status(HttpStatus.OK), true, response, null);
    }

}
