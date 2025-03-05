package com.banka1.user.DTO.banking;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class CreateAccountByEmployeeDTO {
    @NonNull
    private CreateAccountDTO createAccountDTO;
    @NonNull
    private Long employeeId;
}