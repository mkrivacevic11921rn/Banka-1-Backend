package com.banka1.banking.dto;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class CustomerDTO {
    @NonNull
    private Long id;

    @NonNull
    private String firstName;

    @NonNull
    private String lastName;

    @NonNull
    private Long birthDate;

    @NonNull
    private String email;

    @NonNull
    private String phoneNumber;

    @NonNull
    private String address;

}