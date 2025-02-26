package com.banka1.user.DTO.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ResetPasswordRequestDTO {
    private String email;
}
