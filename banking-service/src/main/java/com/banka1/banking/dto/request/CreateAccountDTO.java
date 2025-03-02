package com.banka1.banking.dto.request;

import com.banka1.banking.models.helper.AccountStatus;
import com.banka1.banking.models.helper.AccountType;
import com.banka1.banking.models.helper.CurrencyType;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAccountDTO {
    @NotBlank(message = "ID vlasnika racuna ne može biti prazan")
    private Long ownerID;

    @NotBlank(message = "Ne zajebavaj se")
    @Size(min = 2, max = 30, message = "Broj racuna mora imati između 2 i 30 karaktera")
    private String accountNumber;

    @NotBlank(message = "Izaberi valutu racuna")
    private CurrencyType currency;

    @NotBlank(message = "Izaberi tip racuna")
    private AccountType type;

    @NotBlank(message = "Izaberi dnevni limit za potrosnju sredstava sa racuna")
    private Double dailyLimit;

    @NotBlank(message = "Izaberi mesecni limit za potrosnju sredstava sa racuna")
    private Double monthlyLimit;

    @NotBlank(message = "Izaberi status racuna")
    private AccountStatus status;

}
