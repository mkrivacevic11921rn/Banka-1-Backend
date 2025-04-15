package com.banka1.banking.dto.interbank;

import com.banka1.banking.models.helper.IdempotenceKey;
import lombok.Data;

@Data
public class InterbankMessageDTO<T> {

    private IdempotenceKey idempotenceKey;
    private InterbankMessageType messageType;
    private T message;
}