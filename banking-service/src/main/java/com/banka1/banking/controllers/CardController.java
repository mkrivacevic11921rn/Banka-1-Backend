package com.banka1.banking.controllers;

import com.banka1.banking.aspect.CardAuthorization;
import com.banka1.banking.dto.CreateCardDTO;
import com.banka1.banking.dto.UpdateCardDTO;
import com.banka1.banking.dto.UpdateCardLimitDTO;
import com.banka1.banking.models.Card;
import com.banka1.banking.services.CardService;
import com.banka1.banking.utils.ResponseMessage;
import com.banka1.banking.utils.ResponseTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{account_id}")
    @Operation(summary = "Pregled svih kartica", description = "Pregled svih kartica korisnika za traženi račun")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista kartica korisnika za određeni račun."),
            @ApiResponse(responseCode = "404", description = "Nema kartica za traženi račun.")
    })
    @CardAuthorization
    public ResponseEntity<?> getCardsByAccountID(@PathVariable("account_id") int accountId) {
        return getCards(accountId);
    }

    @PostMapping("/")
    @Operation(summary = "Kreiranje kartice", description = "Kreira novu karticu povezanu sa računom korisnika.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kartica uspešno kreirana."),
            @ApiResponse(responseCode = "400", description = "Nevalidni podaci.")
    })
    @CardAuthorization
    public ResponseEntity<?> createCard(@RequestBody CreateCardDTO createCardDTO) {
        try {
            Card card = cardService.createCard(createCardDTO);
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.CREATED), true, Map.of("id", card.getId(), "message", ResponseMessage.CARD_CREATED_SUCCESS.toString()), null);
        } catch (RuntimeException e) {
            log.error("Greška prilikom kreiranja kartice: ", e);
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);
        }
    }

    @PatchMapping("/{card_id}")
    @Operation(summary = "Blokiranje i deblokiranje kartice", description = "Blokiranje i deblokiranje kartice")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kartica uspešno ažurirana"),
            @ApiResponse(responseCode = "400", description = "Nevalidni podaci.")
    })
    @CardAuthorization
    public ResponseEntity<?> blockCard(@PathVariable("card_id") int cardId, @RequestBody UpdateCardDTO updateCardDTO) {
        try {
            cardService.blockCard(cardId, updateCardDTO);
            return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("message", ResponseMessage.CARD_UPDATED_SUCCESS.toString()), null);
        } catch (RuntimeException e) {
            log.error("Greška prilikom ažuriranja kartice: ", e);
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);
        }
    }

    @GetMapping("/admin/{account_id}")
    @Operation(summary = "Pregled svih kartica od strane zaposlenog", description = "Pregled svih kartica za traženi račun od strane zaposlenog")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "L ista kartica za određeni račun."),
            @ApiResponse(responseCode = "404", description = "Nema kartica za traženi račun.")
    })
    @CardAuthorization(employeeOnlyOperation = true)
    public ResponseEntity<?> getAdminCardsByAccountID(@PathVariable("account_id") int accountId) {
        return getCards(accountId);
    }

    @PatchMapping("/admin/{card_id}")
    @Operation(summary = "Aktivacija i deaktivacija kartice od strane zaposlenog", description = "Aktivacija i deaktivacija kartice od strane zaposlenog")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kartica uspešno ažurirana"),
            @ApiResponse(responseCode = "400", description = "Nevalidni podaci.")
    })
    @CardAuthorization(employeeOnlyOperation = true)
    public ResponseEntity<?> activateCard(@PathVariable("card_id") int cardId, @RequestBody UpdateCardDTO updateCardDTO) {
        try {
            cardService.activateCard(cardId, updateCardDTO);
            return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("message", ResponseMessage.CARD_UPDATED_SUCCESS.toString()), null);
        } catch (RuntimeException e) {
            log.error("Greška prilikom ažuriranja kartice: ", e);
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);
        }
    }

    private ResponseEntity<?> getCards(int account_id) {
        try {
            List<Card> cards = cardService.findAllByAccountId(account_id);
            if (cards.isEmpty()) {
                return ResponseTemplate.create(ResponseEntity.status(HttpStatus.NOT_FOUND), false, null, ResponseMessage.CARD_NOT_FOUND.toString());
            }
            return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("cards", cards), null);
        } catch (Exception e) {
            log.error("Greška prilikom trazenja kartica: ", e);
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);
        }
    }

    @PatchMapping("/{card_id}/limit")
    @Operation(summary = "Promena limita kartice", description = "Omogućava korisniku da promeni limit kartice.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Limit kartice uspešno promenjen."),
            @ApiResponse(responseCode = "400", description = "Nevalidni podaci."),
            @ApiResponse(responseCode = "404", description = "Kartica nije pronađena.")
    })
    @CardAuthorization
    public ResponseEntity<?> updateCardLimit(@PathVariable("card_id") Long cardId, @RequestBody UpdateCardLimitDTO updateCardLimitDTO) {
        try {
            cardService.updateCardLimit(cardId, updateCardLimitDTO);
            return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("message", "Limit kartice uspešno ažuriran."), null);
        } catch (RuntimeException e) {
            log.error("Greška prilikom ažuriranja limita kartice: ", e);
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);
        }
    }
}
