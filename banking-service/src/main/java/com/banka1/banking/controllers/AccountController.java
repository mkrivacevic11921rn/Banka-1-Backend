package com.banka1.banking.controllers;

import com.banka1.banking.dto.request.CreateAccountDTO;
import com.banka1.banking.models.Account;
import com.banka1.banking.services.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private AccountService accountService;

    @PostMapping("/")
//    @Authorization(permissions = { Permission.CREATE_EMPLOYEE }, positions = { Position.HR })
    @Operation(summary = "Kreiranje racuna", description = "Dodaje novi racun u sistem.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Račun uspešno kreiran.\n"),
            @ApiResponse(responseCode = "403", description = "Nevalidni podaci")
    })

    public ResponseEntity<?> createAccount(@RequestBody CreateAccountDTO createAccountDTO) {
        Account savedAccount = null;
        try {
            savedAccount = accountService.createAccount(createAccountDTO);
        } catch (RuntimeException e) {
            System.err.println(e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("id vlasnika racuna", savedAccount.getOwnerID());
        data.put("id racuna", savedAccount.getId());
        data.put("message", "Račun uspešno kreiran.\n");
        response.put("data", data);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/")
    @Operation(summary = "Dohvatanje svih računa",
            description = "Vraća listu svih računa u sistemu.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista računa uspešno dohvaćena"),
            @ApiResponse(responseCode = "404", description = "Nema dostupnih računa")
    })

    public ResponseEntity<?> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();

        if (accounts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nema dostupnih računa.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", accounts);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Dohvatanje računa specificnog korisnika", description = "Vraća sve račune vezane za određenog korisnika.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Računi uspešno dohvaćeni"),
            @ApiResponse(responseCode = "404", description = "Nema računa za datog korisnika")
    })

    public ResponseEntity<?> getAccountsByUser(@PathVariable Long userId) {
        List<Account> accounts = accountService.getAccountsByOwnerId(userId);

        if (accounts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nema računa za ovog korisnika.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", accounts);

        return ResponseEntity.ok(response);
    }
}
