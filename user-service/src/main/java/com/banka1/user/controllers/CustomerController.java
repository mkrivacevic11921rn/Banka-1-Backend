package com.banka1.user.controllers;

import com.banka1.user.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer")
@Tag(name = "Musterije")
public class CustomerController {
    private final CustomerService customerService;

    @Operation(
            summary = "Dobavljanje informacija o musteriji datog ID-a"
    )
    @GetMapping("{id}")
    //@Authorization(permissions = { Permission.READ_CUSTOMER }, allowIdFallback = true )
    public ResponseEntity<?> getById(
//            @RequestHeader("Authorization")
//            String authorization,
            @Parameter(required = true, example = "1")
            @PathVariable String id
    ) {
        try {
            var customer = customerService.findById(id);
            if (customer == null)
                return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(Map.of(
                        "success", false,
                        "error", "Korisnik nije pronadjen."
                ));
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", customer
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}
