package com.banka1.banking.controllers;

import com.banka1.banking.dto.InternalTransferDTO;
import com.banka1.banking.dto.MoneyTransferDTO;
import com.banka1.banking.models.Account;
import com.banka1.banking.services.TransferService;
import com.banka1.banking.utils.ResponseTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/internal-transfer")
    public ResponseEntity<?> internalTransfer(@RequestBody InternalTransferDTO transferDTO){

        try {

            if (!transferService.validateInternalTransfer(transferDTO)){
                return ResponseTemplate.create(ResponseEntity.status(HttpStatus.BAD_REQUEST),
                        false, null, "Nevalidni podaci ili nedovoljno sredstava.");
            }

            transferService.createInternalTransfer(transferDTO);

//            Long fromAccountId = transferDTO.getFromAccountId();
//            Long toAccountId = transferDTO.getToAccountId();
//            Double amount = transferDTO.getAmount();

            return ResponseTemplate.create(ResponseEntity.ok(),true, Map.of("message","Interni prenos uspešno kreiran."),null);


        } catch (Exception e) {
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);

        }

    }

    @PostMapping("/money-transfer")
    public ResponseEntity<?> validateMoneyTransfer(@RequestBody MoneyTransferDTO transferDTO){

        try {

            if (!transferService.validateMoneyTransfer(transferDTO)){
                return ResponseTemplate.create(ResponseEntity.status(HttpStatus.BAD_REQUEST),
                        false, null, "Nevalidni podaci ili nedovoljno sredstava.");
            }

            transferService.createMoneyTransfer(transferDTO);

//            Long fromAccountId = transferDTO.getFromAccountId();
//            Long toAccountId = transferDTO.getToAccountId();
//            Double amount = transferDTO.getAmount();

            return ResponseTemplate.create(ResponseEntity.ok(),true, Map.of("message","Prenos novca uspešno kreiran."),null);

        } catch (Exception e) {
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);

        }
    }


}
