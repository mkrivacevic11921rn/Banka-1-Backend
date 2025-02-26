package com.banka1.user.DTO.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SetPasswordDTO {
    private String code;
    private String password;
}
