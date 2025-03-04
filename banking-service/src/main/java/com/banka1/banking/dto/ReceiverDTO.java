package com.banka1.banking.dto;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ReceiverDTO {

    private Long customerId;

    private String accountNumber;

    private String firstName;

    private String lastName;

    private String address;

}
