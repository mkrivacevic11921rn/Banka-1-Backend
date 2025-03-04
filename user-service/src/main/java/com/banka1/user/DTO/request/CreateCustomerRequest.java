package com.banka1.user.DTO.request;

import com.banka1.user.DTO.banking.CreateAccountWithoutOwnerIdDTO;
import com.banka1.user.model.helper.Gender;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class CreateCustomerRequest {
    @NonNull
    private String firstName;

    @NonNull
    private String lastName;

    @NonNull
    private String username;

    @NonNull
    private String password;

    @NonNull
    private Long birthDate;

    @NonNull
    private Gender gender;

    @NonNull
    private String email;

    @NonNull
    private String phoneNumber;

    @NonNull
    private String address;

    @NonNull
    private CreateAccountWithoutOwnerIdDTO accountInfo;
}
