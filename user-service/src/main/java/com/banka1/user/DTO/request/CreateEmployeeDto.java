package com.banka1.user.DTO.request;

import com.banka1.user.model.helper.Department;
import com.banka1.common.model.Permission;
import com.banka1.common.model.Position;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateEmployeeDto {

    @NotBlank(message = "Ime ne može biti prazno")
    @Size(min = 2, max = 50, message = "Ime mora imati između 2 i 50 karaktera")
    private String firstName;

    @NotBlank(message = "Prezime ne može biti prazno")
    @Size(min = 2, max = 50, message = "Prezime mora imati između 2 i 50 karaktera")
    private String lastName;

    @NotNull(message = "Datum rođenja je obavezan")
    private LocalDate birthDate;

    @NotBlank(message = "Pol je obavezan")
    private String gender;

    @NotBlank(message = "Email je obavezan")
    @Email(message = "Email mora biti validan")
    private String email;

    @NotBlank(message = "Broj telefona je obavezan")
    private String phoneNumber;

    @NotBlank(message = "Adresa je obavezna")
    private String address;

    @NotBlank(message = "Username je obavezan")
    private String username;

    @NotBlank(message = "Pozicija je obavezna")
    private Position position;

    @NotBlank(message = "Departman je obavezan")
    private Department department;

    private Boolean active = true; // Default vrednost

    private Boolean isAdmin = false; // default vrednost

    private List<Permission> permissions;
}
