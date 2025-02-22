package com.banka1.user.DTO.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
