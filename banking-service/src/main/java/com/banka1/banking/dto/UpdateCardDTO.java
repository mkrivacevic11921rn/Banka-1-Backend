package com.banka1.banking.dto;

import com.banka1.banking.models.helper.CardStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCardDTO {
    private CardStatus status;
}
