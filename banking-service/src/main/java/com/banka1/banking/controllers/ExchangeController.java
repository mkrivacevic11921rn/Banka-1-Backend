package com.banka1.banking.controllers;

import com.banka1.banking.dto.ExchangeMoneyTransferDTO;
import com.banka1.banking.dto.InternalTransferDTO;
import com.banka1.banking.services.ExchangeService;
import com.banka1.banking.services.TransferService;
import com.banka1.banking.utils.ResponseTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/exchange-transfer")
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @PostMapping
    public ResponseEntity<?> exchangeMoneyTransfer(@RequestBody ExchangeMoneyTransferDTO exchangeMoneyTransferDTO){

        try {
            if(!exchangeService.validateExchangeTransfer(exchangeMoneyTransferDTO)){
                return ResponseTemplate.create(ResponseEntity.status(HttpStatus.BAD_REQUEST),
                        false,null,"Nevalidni podaci ili nedovoljno sredstava.");
            }

            exchangeService.createExchangeTransfer(exchangeMoneyTransferDTO);

            return ResponseTemplate.create(ResponseEntity.ok(),true, Map.of("message","Interni prenos sa konverzijom uspesno izvršen."),null);

        } catch (Exception e) {
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);
        }

    }

}
