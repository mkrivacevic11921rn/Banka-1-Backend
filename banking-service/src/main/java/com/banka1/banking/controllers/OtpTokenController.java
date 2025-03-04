package com.banka1.banking.controllers;

import com.banka1.banking.dto.OtpTokenDTO;
import com.banka1.banking.models.Account;
import com.banka1.banking.models.Currency;
import com.banka1.banking.models.Transfer;
import com.banka1.banking.models.helper.TransferStatus;
import com.banka1.banking.repository.TransferRepository;
import com.banka1.banking.services.OtpTokenService;
import com.banka1.banking.utils.ResponseTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
@Tag(name = "OTP verifikacija", description = "Ruta za validaciju kreiranog transfera")
public class OtpTokenController {

    private final OtpTokenService otpTokenService;
    private final TransferRepository transferRepository;

    @Operation(
            summary = "Verifikacija OTP koda",
            description = "Proverava da li je uneti OTP kod validan i još uvek važeći."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP validan, transakcija izvršena.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"success\": true, \"data\": { \"message\": \"OTP validan, transakcija izvršena.\" } }"))
            ),
            @ApiResponse(responseCode = "401", description = "Nevalidan OTP kod ili je već iskorišćen.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"success\": false, \"error\": \"Nevalidan OTP kod ili je već iskorišćen.\" }"))
            ),
            @ApiResponse(responseCode = "408", description = "OTP kod je istekao.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"success\": false, \"error\": \"OTP kod je istekao.\" }"))
            )
    })
    @PostMapping("/verification")
    public ResponseEntity<?> verifyOtp(@RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Podaci za verifikaciju OTP koda",
            required = true,
            content = @Content(schema = @Schema(implementation = OtpTokenDTO.class),
                    examples = @ExampleObject(value = "{ \"transakcijaId\": 1, \"otpKod\": \"123456\" }")
            )) OtpTokenDTO otpTokenDTO) {

        try {
            Long transferId = otpTokenDTO.getTransferId();
            String otpCode = otpTokenDTO.getOtpCode();

            if (otpTokenService.isOtpExpired(transferId)) {
                return ResponseTemplate.create(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT),
                        false, null, "OTP kod je istekao.");
            }

            if (!otpTokenService.isOtpValid(transferId, otpCode)) {
                return ResponseTemplate.create(ResponseEntity.status(HttpStatus.UNAUTHORIZED),
                        false, null, "Nevalidan OTP kod ili je već iskorišćen.");
            }

            // PODACI ZA OBRADU TRANSAKCIJA KOJE TREBA POZVATI OVDE

            Optional<Transfer> optionalTransfer = transferRepository.findById(transferId);

            if(optionalTransfer.isPresent()){
                Transfer transfer = optionalTransfer.get();
                if(transfer.getStatus().equals(TransferStatus.CANCELLED)){
                    return ResponseTemplate.create(ResponseEntity.status(HttpStatus.BAD_REQUEST),
                            false,null,"Transfer je otkazan.");
                }
                otpTokenService.markOtpAsUsed(transferId, otpCode);

                Account accountTo = transfer.getToAccountId();
                Account accountFrom = transfer.getFromAccountId();
                Currency currencyTo = transfer.getToCurrency();
                Currency currencyFrom = transfer.getFromCurrency();
                Double amount = transfer.getAmount();


                // TRANSFER OBELEZEN KAO USPESNO ZAVRSEN
                // transfer.get().setStatus(TransferStatus.COMPLETED);

                // TRANSFER OBELEZEN KAO NEUSPEO AKO TRANSAKCIJE NISU USPELE
                // transfer.setStatus(TransferStatus.FAILED);

                transferRepository.save(transfer);
            }


            return ResponseTemplate.create(ResponseEntity.ok(),
                    true, Map.of("message", "OTP validan, transakcija izvršena."), null);

        } catch (Exception e) {
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);
        }
    }
}

