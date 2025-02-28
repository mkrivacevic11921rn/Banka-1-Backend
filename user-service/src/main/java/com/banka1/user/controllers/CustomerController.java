package com.banka1.user.controllers;

import com.banka1.user.DTO.CustomerDTO.CustomerDTO;
import com.banka1.user.DTO.request.SetPasswordDTO;
import com.banka1.user.aspect.Authorization;
import com.banka1.user.model.Customer;
import com.banka1.user.model.helper.Permission;
import com.banka1.user.service.CustomerService;
import com.banka1.user.utils.ResponseTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customer")
@Tag(name = "Musterije")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(
            summary = "Dobavljanje informacija o musteriji datog ID-a"
    )
    @GetMapping("/{id}")
    @Authorization(permissions = { Permission.READ_CUSTOMER }, allowIdFallback = true )
    public ResponseEntity<?> getById(
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

    @PostMapping
    @Authorization(permissions = { Permission.CREATE_CUSTOMER }, allowIdFallback = true )
    @Operation(summary = "Create a new customer", description = "Creates a new customer and returns the created customer ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Zaposleni uspešno kreiran"),
            @ApiResponse(responseCode = "403", description = "Nemaš permisije za ovu akciju")
    })
    public ResponseEntity<?> createCustomer(
            @RequestBody @Parameter(description = "Customer data for creation") CustomerDTO customerDTO) {
        Customer savedCustomer = customerService.createCustomer(customerDTO);
        return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("id", savedCustomer.getId()), null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user.customer.edit')")
    @Authorization(permissions = { Permission.EDIT_CUSTOMER }, allowIdFallback = true )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<?> updateCustomer(
            @PathVariable @Parameter(description = "Customer ID to update") Long id,
            @RequestBody @Parameter(description = "Updated customer data") CustomerDTO customerDTO) {
        Optional<Customer> updatedCustomer = customerService.updateCustomer(id, customerDTO);

        if (updatedCustomer.isPresent()) {
            return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("message", "Podaci korisnika ažurirani"), null);
        } else {
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.NOT_FOUND), false, null, "Korisnik nije pronađen");
        }

    }

    @DeleteMapping("/{id}")
    @Authorization(permissions = { Permission.DELETE_CUSTOMER }, allowIdFallback = true )
    @Operation(summary = "Delete customer", description = "Deletes a customer by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<?> deleteCustomer(
            @PathVariable @Parameter(description = "Customer ID to delete") Long id) {
        boolean deleted = customerService.deleteCustomer(id);

        if (deleted) {
            return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("message", "Korisnik uspešno obrisan"), null);
        } else {
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.NOT_FOUND), false, null, "Korisnik nije pronađen");
        }

    }

    @PutMapping("/{id}/permissions")
    @Authorization(permissions = { Permission.SET_CUSTOMER_PERMISSION }, allowIdFallback = true )
    @Operation(summary = "Update customer permissions", description = "Updates the permissions of a specific customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - empty permissions list"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<?> updateCustomerPermissions(
            @PathVariable @Parameter(description = "Customer ID") Long id,
            @RequestBody @Parameter(description = "Permissions list") Map<String, List<Permission>> permissionsMap) {
        List<Permission> permissions = permissionsMap.get("permissions");

        Optional<Customer> updatedCustomer = customerService.updateCustomerPermissions(id, permissions);

        if (updatedCustomer.isPresent()) {
            return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("message", "Permisije ažurirane"), null);
        } else {
            return ResponseTemplate.create(ResponseEntity.status(HttpStatus.NOT_FOUND), false, null, "Korisnik nije pronađen");
        }

    }

    @PutMapping("/set-password")
    @Operation(summary = "Set customer password", description = "Sets the password for a specific customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password set successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - missing required fields")
    })
    public ResponseEntity<?> setPassword(@RequestBody SetPasswordDTO setPasswordDTO) {
        try {
            customerService.setPassword(setPasswordDTO);
            return ResponseTemplate.create(ResponseEntity.ok(), true, Map.of("message", "Lozinka uspešno postavljena"), null);
        } catch (Exception e) {
            return ResponseTemplate.create(ResponseEntity.badRequest(), e);
        }
    }


}
