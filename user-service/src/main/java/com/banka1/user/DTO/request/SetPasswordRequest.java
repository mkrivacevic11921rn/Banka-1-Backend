package com.banka1.user.DTO.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SetPasswordRequest {
    private String code;
    private String password;
}
