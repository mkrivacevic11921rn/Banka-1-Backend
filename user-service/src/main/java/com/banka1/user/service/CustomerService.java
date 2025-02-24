package com.banka1.user.service;

import com.banka1.user.dto.CustomerDTO;
import com.banka1.user.mapper.CustomerMapper;
import com.banka1.user.model.Customer;
import com.banka1.user.model.helper.Permission;
import com.banka1.user.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Tag(name = "Customer Service", description = "Business logic for customer operations")
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Creates a new customer.
     * @param customerDTO The customer data transfer object.
     * @return The created customer entity.
     */
    @Operation(summary = "Create a customer", description = "Creates a new customer with hashed password and stores it in the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer successfully created",
                    content = @Content(schema = @Schema(implementation = Customer.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public Customer createCustomer(CustomerDTO customerDTO) {
        Customer customer = CustomerMapper.dtoToCustomer(customerDTO);

        // Generate salt and hash password
        String salt = generateSalt();
        String saltedPassword = salt + customer.getPassword();
        String hashedPassword = passwordEncoder.encode(saltedPassword);

        customer.setPassword(hashedPassword);
        customer.setSaltPassword(salt);

        return customerRepository.save(customer);
    }

    /**
     * Updates an existing customer's details.
     * @param id The customer ID.
     * @param customerDTO The updated customer data.
     * @return The updated customer, if found.
     */
    @Operation(summary = "Update a customer", description = "Updates an existing customer’s details based on the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public Optional<Customer> updateCustomer(Long id, CustomerDTO customerDTO) {
        return customerRepository.findById(id).map(customer -> {
            if (customerDTO.getIme() != null) {
                customer.setFirstName(customerDTO.getIme());
            }
            if (customerDTO.getPrezime() != null) {
                customer.setLastName(customerDTO.getPrezime());
            }
            if (customerDTO.getDatum_rodjenja() != null) {
                customer.setBirthDate(LocalDate.parse(customerDTO.getDatum_rodjenja()));
            }
            if (customerDTO.getPol() != null) {
                customer.setGender(customerDTO.getPol().equalsIgnoreCase("M") ? Gender.MALE : Gender.FEMALE);
            }
            if (customerDTO.getEmail() != null) {
                customer.setEmail(customerDTO.getEmail());
            }
            if (customerDTO.getBroj_telefona() != null) {
                customer.setPhoneNumber(customerDTO.getBroj_telefona());
            }
            if (customerDTO.getAdresa() != null) {
                customer.setAddress(customerDTO.getAdresa());
            }
            if (customerDTO.getPassword() != null) {
                String salt = generateSalt();
                String saltedPassword = salt + customerDTO.getPassword();
                customer.setPassword(passwordEncoder.encode(saltedPassword));
                customer.setSaltPassword(salt);
            }

            return customerRepository.save(customer);
        });
    }

    /**
     * Deletes a customer by ID.
     * @param id The customer ID.
     * @return True if the customer was deleted, otherwise throws an exception.
     */
    @Operation(summary = "Delete a customer", description = "Deletes a customer from the database based on ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public boolean deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik nije pronađen"));

        customerRepository.delete(customer);
        return true;
    }

    /**
     * Updates the permissions of a customer.
     * @param id The customer ID.
     * @param permissions The list of permissions to assign.
     * @return The updated customer with new permissions.
     */
    @Operation(summary = "Update customer permissions", description = "Updates the permissions for a specific customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - empty permissions list"),
            @ApiResponse(responseCode = "404", description = "Customer not
