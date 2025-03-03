package com.banka1.banking.dto.request;

import com.banka1.banking.models.helper.AccountStatus;
import com.banka1.banking.models.helper.AccountSubtype;
import com.banka1.banking.models.helper.AccountType;
import com.banka1.banking.models.helper.CurrencyType;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CreateAccountDTO {
    @NotNull(message = "ID vlasnika racuna ne može biti prazan")
    private Long ownerID;

    @NotNull(message = "Izaberi valutu za racun")
    private CurrencyType currency;

    @NotNull(message = "Izaberi tip racuna")
    private AccountType type;

    @NotNull(message = "Izaberi podtip racuna")
    private AccountSubtype subtype;

    @NotNull(message = "Izaberi dnevni limit za potrosnju sredstava sa racuna")
    private Double dailyLimit;

    @NotNull(message = "Izaberi mesecni limit za potrosnju sredstava sa racuna")
    private Double monthlyLimit;

    @NotNull(message = "Izaberi status racuna")
    private AccountStatus status;

    private Double balance;

}
