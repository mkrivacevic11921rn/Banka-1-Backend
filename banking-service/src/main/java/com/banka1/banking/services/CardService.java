package com.banka1.banking.services;

import com.banka1.banking.dto.response.CardResponse;
import com.banka1.banking.models.Card;
import com.banka1.banking.repository.CardRepository;
import org.springframework.stereotype.Service;

@Service
public class CardService {
    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public CardResponse findById(String id) {
        return cardRepository.findById(Long.parseLong(id)).map(CardService::getCardResponse).orElse(null);
    }

    public static CardResponse getCardResponse(Card card) {
        return new CardResponse(
                AccountService.getAccountResponse(card.getAccount()));
    }
}
