package com.banka1.banking.dto;

import com.banka1.banking.models.helper.CardBrand;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCardLimitDTO {
    private Double newLimit;
}
