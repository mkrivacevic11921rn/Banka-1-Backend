package com.banka1.user.DTO.request;

import com.banka1.user.model.helper.Gender;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateCustomerRequest {
    private String firstName;

    private String lastName;

    private String username;

    private Long birthDate;

    private Gender gender;

    private String email;

    private String phoneNumber;

    private String address;
}
