package com.banka1.banking.controllers;

import com.banka1.banking.models.Transfer;
import com.banka1.banking.models.helper.TransferType;
import com.banka1.banking.services.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/internal-transfer")
    public ResponseEntity<String> processInternalTransfer(@RequestBody Transfer transfer) {


        try {
            //ovde se ulazi kada je OTP validan
                String result = transferService.processTransfer(transfer.getId());
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                // 3. Ako dođe do greške (npr. nema dovoljno sredstava), vraćamo odgovarajući status
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

}