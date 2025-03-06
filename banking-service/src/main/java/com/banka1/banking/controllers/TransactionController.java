package com.banka1.banking.controllers;

import com.banka1.banking.aspect.AccountAuthorization;
import com.banka1.banking.models.Transaction;
import com.banka1.banking.services.AccountService;
import com.banka1.banking.services.TransactionService;
import com.banka1.banking.services.implementation.AuthService;
import com.banka1.banking.utils.ResponseTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transaction API", description = "API za upravljanje transakcijama")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AuthService authService;

    @GetMapping("/")
    @Operation(summary = "Dobavljanje svih transakcija", description = "Vraća listu svih transakcija u sistemu.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Uspešno dobavljanje transakcija"),
            @ApiResponse(responseCode = "400", description = "Greška pri dobavljanju transakcija")
    })
    @AccountAuthorization
    public ResponseEntity<?> getAllTransactions(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            Long userId = authService.parseToken(authService.getToken(authorization)).get("id", Long.class);
            List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("data", transactions);
            return ResponseTemplate.create(ResponseEntity.ok(), true, response, null);
        } catch (Exception e) {
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);
        }
    }

    @GetMapping("/admin/{userId}")
    @Operation(summary = "Dobavljanje svih transakcija za korisnika", description = "Vraća listu svih transakcija za korisnika sa datim ID-em.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Uspešno dobavljanje transakcija"),
            @ApiResponse(responseCode = "400", description = "Greška pri dobavljanju transakcija")
    })
    @AccountAuthorization(employeeOnlyOperation = true)
    public ResponseEntity<?> getAllTransactionsAdmin(@PathVariable Long userId, @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("data", transactions);
            return ResponseTemplate.create(ResponseEntity.ok(), true, response, null);
        } catch (Exception e) {
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);
        }
    }
}
