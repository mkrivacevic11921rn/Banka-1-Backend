package com.banka1.banking.dto;

import com.banka1.banking.models.AuthorizedPerson;
import com.banka1.banking.models.helper.CardType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCardDTO {
    private Long accountID;
    private CardType cardType;
    private AuthorizedPerson authorizedPerson;
}
