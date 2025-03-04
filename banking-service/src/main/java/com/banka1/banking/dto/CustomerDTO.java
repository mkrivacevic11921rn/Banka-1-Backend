package com.banka1.banking.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Data
public class CustomerDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String birthDate;
    private String gender;
    private String email;
    private String phoneNumber;
    private String address;
    private List<String> permissions;

}
