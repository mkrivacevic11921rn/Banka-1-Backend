package com.banka1.banking.controllers;

import com.banka1.banking.aspect.AccountAuthorization;
import com.banka1.banking.dto.request.CreateAccountDTO;
import com.banka1.banking.dto.request.CreateLoanDTO;
import com.banka1.banking.models.Account;
import com.banka1.banking.models.Loan;
import com.banka1.banking.services.LoanService;
import com.banka1.banking.services.implementation.AuthService;
import com.banka1.banking.utils.ResponseMessage;
import com.banka1.banking.utils.ResponseTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/loans")
@Tag(name = "Loan API", description = "API za pozive vezane za kredite i rate")
public class LoanController {
    @Autowired
    private LoanService loanService;
    @Autowired
    private AuthService authService;

    @PostMapping("/request")
    @Operation(summary = "Kreiranje zahteva za kredit",
            description = "Dodaje novi kredit u sistem i cuva se u bazi pod statusom na cekanju.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kredit uspešno kreiran.\n"),
            @ApiResponse(responseCode = "403", description = "Nevalidni podaci")
    })
    public ResponseEntity<?> createLoan(@Valid @RequestBody CreateLoanDTO createLoanDTO) {
        Loan newLoan = null;
        try {
            newLoan = loanService.createLoan(createLoanDTO);
        } catch (RuntimeException e) {
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);
        }

        if (newLoan == null) {
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.SEE_OTHER), false, null, ResponseMessage.ACCOUNT_NOT_FOUND.getMessage());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("broj racuna", newLoan.getId());
        response.put("message", "Kredit uspešno kreiran.\n");

        return ResponseTemplate.create(ResponseEntity.status(HttpStatus.CREATED), true, response, null);
    }
}
