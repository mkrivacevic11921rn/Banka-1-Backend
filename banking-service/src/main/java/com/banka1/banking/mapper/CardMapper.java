package com.banka1.banking.mapper;

import com.banka1.banking.dto.AuthorizedPersonDTO;
import com.banka1.banking.dto.CreateCardDTO;
import com.banka1.banking.models.AuthorizedPerson;
import com.banka1.banking.models.Card;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Component
public class CardMapper {
    private static final Random rand = new Random();

    public Card dtoToCard(CreateCardDTO dto) {
        Card card = new Card();

        card.setCardType(dto.getCardType());
        card.setCardBrand(dto.getCardBrand());
        card.setCreatedAt(Instant.now().getEpochSecond());
        card.setExpirationDate(Instant.now().plus(5, ChronoUnit.YEARS).getEpochSecond());
        card.setActive(true);
        card.setBlocked(false);
        card.setCardLimit(1000000.0);

        String cardNumber = switch (card.getCardBrand()) {
            case VISA -> "4" + generateRandomNumeric(15);
            case MASTERCARD -> "51" + generateRandomNumeric(14);
            case DINA_CARD -> "9891" + generateRandomNumeric(12);
            case AMERICAN_EXPRESS -> "34" + generateRandomNumeric(14);
        };

        card.setCardNumber(cardNumber);
        card.setCardCvv(generateRandomNumeric(3));

        return card;
    }

    public AuthorizedPerson dtoToAuthorizedPerson(AuthorizedPersonDTO dto) {
        AuthorizedPerson authorizedPerson = new AuthorizedPerson();

        authorizedPerson.setFirstName(dto.getFirstName());
        authorizedPerson.setLastName(dto.getLastName());
        authorizedPerson.setPhoneNumber(dto.getPhoneNumber());
        authorizedPerson.setBirthDate(dto.getBirthDate());

        return authorizedPerson;
    }

    private static String generateRandomNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(rand.nextInt(10));
        }
        return builder.toString();
    }
}
