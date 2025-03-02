package com.banka1.banking.controllers;

import com.banka1.banking.dto.CreateCardDTO;
import com.banka1.banking.dto.UpdateCardDTO;
import com.banka1.banking.models.Card;
import com.banka1.banking.services.CardService;
import com.banka1.banking.utils.ResponseTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/cards")
@Tag(name = "Card API", description = "API za upravljanje karticama")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/")
//    @Authorization(permissions = { Permission.READ_EMPLOYEE }, allowIdFallback = true )
    @Operation(summary = "Pregled svih kartica", description = "Pregled svih kartica korisnika za traženi račun")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista kartica korisnika za određeni račun."),
            @ApiResponse(responseCode = "404", description = "Nema kartica za traženi račun.")
    })
    public ResponseEntity<?> getCardsByAccountID(@RequestParam int account_id) {
        try {
            List<Card> cards = cardService.findAllByAccountId(account_id);
            if (cards.isEmpty())
                return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(Map.of(
                        "success", false,
                        "error", "Nema kartica za traženi račun."
                ));
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", cards
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/")
//    @Authorization(permissions = { Permission.CREATE_EMPLOYEE }, positions = { Position.HR })
    @Operation(summary = "Kreiranje kartice", description = "Kreira novu karticu povezanu sa računom korisnika.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kartica uspešno kreirana."),
            @ApiResponse(responseCode = "400", description = "Nevalidni podaci.")
    })
    public ResponseEntity<?> createCard(@RequestBody CreateCardDTO createCardDTO) {
        Card card = null;
        try {
            card = cardService.createCard(createCardDTO);
        } catch (RuntimeException e) {
            log.error("e: ", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("id", card.getId());
        data.put("message", "Kartica uspešno kreirana.");
        response.put("data", data);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{card_id}")
    @Operation(summary = "Blokiranje i deblokiranje kartice", description = "Blokiranje i deblokiranje kartice")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kartica uspešno ažurirana"),
            @ApiResponse(responseCode = "400", description = "Nevalidni podaci.")
    })
    public ResponseEntity<?> blockCard(@PathVariable int card_id, @RequestBody UpdateCardDTO updateCardDTO) {
        try {
            cardService.updateCard(card_id, updateCardDTO);
            return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("message", "Kartica uspešno ažurirana"), null);
        } catch (Exception e) {
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);
        }
    }

    @GetMapping("/admin/{account_id}")
//    @Authorization(permissions = { Permission.READ_EMPLOYEE }, allowIdFallback = true )
    @Operation(summary = "Pregled svih kartica od strane zaposlenog", description = "Pregled svih kartica za traženi račun od strane zaposlenog")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista kartica za određeni račun."),
            @ApiResponse(responseCode = "404", description = "Nema kartica za traženi račun.")
    })
    public ResponseEntity<?> getAdminCardsByAccountID(@PathVariable int account_id) {
        try {
            List<Card> cards = cardService.findAllByAccountId(account_id);
            if (cards.isEmpty())
                return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(Map.of(
                        "success", false,
                        "error", "Nema kartica za traženi račun."
                ));
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", cards
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/admin/{account_id}")
    @Operation(summary = "Aktivacija i deaktivacija kartice od strane zaposlenog", description = "Aktivacija i deaktivacija kartice od strane zaposlenog")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kartica uspešno ažurirana"),
            @ApiResponse(responseCode = "400", description = "Nevalidni podaci.")
    })
    public ResponseEntity<?> activateCard(@PathVariable int account_id, @RequestParam int card_id, @RequestBody UpdateCardDTO updateCardDTO) {
        try {
            cardService.updateCard(card_id, updateCardDTO);
            return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("message", "Kartica uspešno ažurirana"), null);
        } catch (Exception e) {
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);
        }
    }
}
