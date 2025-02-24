package com.banka1.user.controllers;

import com.banka1.user.dto.CustomerDTO;
import com.banka1.user.model.Customer;
import com.banka1.user.model.helper.Permission;
import com.banka1.user.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users/customers")
@Tag(name = "Customer Management", description = "APIs for managing customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user.customer.create')")
    @Operation(summary = "Create a new customer", description = "Creates a new customer and returns the created customer ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer successfully created",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<?> createCustomer(
            @RequestBody @Parameter(description = "Customer data for creation") CustomerDTO customerDTO) {
        Customer savedCustomer = customerService.createCustomer(customerDTO);
        return ResponseEntity.ok().body(new ApiResponse(true, savedCustomer.getId(), "Mušterija uspešno kreirana"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user.customer.edit')")
    @Operation(summary = "Update customer", description = "Updates the details of an existing customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<?> updateCustomer(
            @PathVariable @Parameter(description = "Customer ID to update") Long id,
            @RequestBody @Parameter(description = "Updated customer data") CustomerDTO customerDTO) {
        Optional<Customer> updatedCustomer = customerService.updateCustomer(id, customerDTO);

        if (updatedCustomer.isPresent()) {
            return ResponseEntity.ok().body(new ApiResponse(true, "Podaci korisnika ažurirani"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Korisnik nije pronađen"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user.customer.delete')")
    @Operation(summary = "Delete customer", description = "Deletes a customer by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<?> deleteCustomer(
            @PathVariable @Parameter(description = "Customer ID to delete") Long id) {
        boolean deleted = customerService.deleteCustomer(id);

        if (deleted) {
            return ResponseEntity.ok().body(new ApiResponse(true, "Korisnik uspešno obrisan"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Korisnik nije pronađen"));
        }
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('user.customer.set_permissions')")
    @Operation(summary = "Update customer permissions", description = "Updates the permissions of a specific customer.")
    @ApiResponses(value = {
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
            return ResponseEntity.ok().body(new ApiResponse(true, "Permisije ažurirane"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Korisnik nije pronađen"));
        }
    }

    static class ApiResponse {
        private boolean success;
        private Object data;

        public ApiResponse(boolean success, Object data) {
            this.success = success;
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public Object getData() {
            return data;
        }
    }
}
