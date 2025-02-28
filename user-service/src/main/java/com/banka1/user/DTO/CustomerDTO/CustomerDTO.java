package com.banka1.user.DTO.CustomerDTO;

import com.banka1.user.model.Customer;
import com.banka1.user.model.helper.Gender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CustomerDTO {
    private String ime;
    private String prezime;
    private String username;
    private String datum_rodjenja;
    private String pol;
    private String email;
    private String broj_telefona;
    private String adresa;
    private String password;
}
