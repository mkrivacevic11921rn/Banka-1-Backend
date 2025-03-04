package com.banka1.banking.controllers;

import com.banka1.banking.models.Receiver;
import com.banka1.banking.services.ReceiverService;
import com.banka1.banking.utils.ResponseTemplate;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/receiver")
@RequiredArgsConstructor
public class ReceiverController {

    private final ReceiverService receiverService;

    @GetMapping("/{customerId}")
    public ResponseEntity<?> getReceivers(@PathVariable Long customerId){
        try {
            List<Receiver> receivers = receiverService.getReceiverByCustomer(customerId);
            return ResponseTemplate.create(ResponseEntity.ok(),true,Map.of("receivers",receivers),null);
        } catch (Exception e){
            return ResponseTemplate.create(ResponseEntity.badRequest(),e);
        }
    }

}
