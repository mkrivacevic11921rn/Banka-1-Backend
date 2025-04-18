package com.banka1.banking.controllers;

import com.banka1.banking.dto.CreateEventDTO;
import com.banka1.banking.dto.interbank.InterbankMessageType;
import com.banka1.banking.dto.interbank.otc.InterbankOTCOfferDTO;
import com.banka1.banking.models.Event;
import com.banka1.banking.services.EventExecutorService;
import com.banka1.banking.services.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/otctrade/interbank")
@RequiredArgsConstructor
public class OTCInterbankController {

    private final ObjectMapper objectMapper;
    private final EventService eventService;
    private final EventExecutorService eventExecutorService;

    @PostMapping("/offer")
    public ResponseEntity<?> receiveOTCOfferFromTrading(@RequestBody InterbankOTCOfferDTO offer) {
        try {
            String payload = objectMapper.writeValueAsString(offer);

            String targetUrl = "http://banka4/api/otctrade/interbank/offer";

            Event event = eventService.createEvent(
                    new CreateEventDTO(InterbankMessageType.NEW_TX, payload, targetUrl)
            );

            eventExecutorService.attemptEventAsync(event);

            return ResponseEntity.status(201).body("Event kreiran i poslat banci 4");

        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Greška: " + ex.getMessage());
        }
    }
}