package com.banka1.user.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetPasswordRequest {
    @NotBlank
    private String token;
    @NotBlank
    private String password;
}
