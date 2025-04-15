package com.banka1.banking.dto;

import com.banka1.banking.models.helper.IdempotenceKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class CreateEventDTO {

    private String messageType;

    private String payload;

    private String url;
}
