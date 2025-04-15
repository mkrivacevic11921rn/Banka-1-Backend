package com.banka1.banking.dto.interbank.rollbacktx;

import com.banka1.banking.models.helper.IdempotenceKey;
import lombok.Data;

@Data
public class RollbackTransactionDTO {
    private IdempotenceKey transactionId;
}
