package com.banka1.user.mapper;

import com.banka1.user.DTO.CustomerDTO.CustomerDTO;
import com.banka1.user.model.Customer;
import com.banka1.user.model.helper.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;

/**
 * Mapper class to convert between Customer and CustomerDTO.
 */
@Tag(name = "Customer Mapper", description = "Utility for converting between Customer and CustomerDTO.")
public class CustomerMapper {

    /**
     * Converts a CustomerDTO object to a Customer entity.
     *
     * @param dto The CustomerDTO object.
     * @return The converted Customer entity.
     */
    @Schema(description = "Converts CustomerDTO to Customer entity.")
    public static Customer dtoToCustomer(CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setFirstName(dto.getIme());
        customer.setLastName(dto.getPrezime());
        customer.setUsername(dto.getUsername());

        if (dto.getDatum_rodjenja() != null) {
            customer.setBirthDate(Long.parseLong(dto.getDatum_rodjenja()));
        }
        if (dto.getPol() != null) {
            customer.setGender(dto.getPol().equalsIgnoreCase("M") ? Gender.MALE : Gender.FEMALE);
        }

        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getBroj_telefona());
        customer.setAddress(dto.getAdresa());
        customer.setPassword(dto.getPassword());

        return customer;
    }

    /**
     * Converts a Customer entity to a CustomerDTO object.
     *
     * @param customer The Customer entity.
     * @return The converted CustomerDTO object.
     */
    @Schema(description = "Converts Customer entity to CustomerDTO.")
    public static CustomerDTO customerToDto(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setIme(customer.getFirstName());
        dto.setPrezime(customer.getLastName());

        if (customer.getBirthDate() != null) {
            dto.setDatum_rodjenja(customer.getBirthDate().toString());
        }
        if (customer.getGender() != null) {
            dto.setPol(customer.getGender() == Gender.MALE ? "M" : "F");
        }

        dto.setEmail(customer.getEmail());
        dto.setBroj_telefona(customer.getPhoneNumber());
        dto.setAdresa(customer.getAddress());

        return dto;
    }
}
