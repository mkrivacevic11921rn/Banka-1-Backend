package com.banka1.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class OtpTokenDTO {

    private Long transferId;
    private String OtpCode;

}

