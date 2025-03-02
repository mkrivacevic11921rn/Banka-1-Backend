package com.banka1.banking.services;

import com.banka1.banking.dto.CreateCardDTO;
import com.banka1.banking.dto.UpdateCardDTO;
import com.banka1.banking.models.Card;
import com.banka1.banking.repository.CardRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CardService {
    private final CardRepository cardRepository;

    private ModelMapper modelMapper;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<Card> findAllByAccountId(int accountId) {
        return cardRepository.findByAccountId((long) accountId).isPresent() ? cardRepository.findByAccountId((long) accountId).get() : null;
    }

    public Card createCard(CreateCardDTO createCardDTO) {
        if(createCardDTO.getAuthorizedPerson() == null && cardRepository.findByAccountId(createCardDTO.getAccountID()).isPresent() && cardRepository.findByAccountId(createCardDTO.getAccountID()).get().size() > 2){
            throw new RuntimeException("Privatni racun moze biti povezan sa najvise dve kartice!");
        }
        else if(createCardDTO.getAuthorizedPerson() != null && cardRepository.findByAccountId(createCardDTO.getAccountID()).isPresent() && cardRepository.findByAccountId(createCardDTO.getAccountID()).get().size() > 5){
            throw new RuntimeException("Poslovni racun moze biti povezan sa najvise pet kartica!");
        }

        Card card = modelMapper.map(createCardDTO, Card.class);
        card.setActive(true);
        card.setBlocked(false);
        card.setCreatedAt(Instant.now().getEpochSecond());
        card.setExpirationDate(Instant.now().plus(5, ChronoUnit.YEARS).getEpochSecond());

        return card;
    }

    public void updateCard(int cardId, UpdateCardDTO updateCardDTO) {
        Card card = cardRepository.findById((long) cardId)
                .orElseThrow(() -> new RuntimeException("Kartica nije pronađena"));

        if(updateCardDTO.getStatus().equals("blokirana")){
            card.setBlocked(true);
        } else if(updateCardDTO.getStatus().equals("odblokirana")){
            card.setBlocked(false);
        } else if(updateCardDTO.getStatus().equals("aktivna")){
            card.setActive(true);
        } else if(updateCardDTO.getStatus().equals("deaktivirana")){
            card.setActive(false);
        }

        cardRepository.save(card);
    }
}
