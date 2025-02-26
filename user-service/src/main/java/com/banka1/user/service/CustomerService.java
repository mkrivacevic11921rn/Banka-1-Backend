package com.banka1.user.service;

import com.banka1.user.DTO.response.CustomerPageResponse;
import com.banka1.user.DTO.response.CustomerResponse;
import com.banka1.user.model.Customer;
import com.banka1.user.model.helper.Gender;
import com.banka1.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerResponse findById(String id) {
        var customerOptional = customerRepository.findById(Long.parseLong(id));
        if (customerOptional.isEmpty())
            return null;
        var customer = customerOptional.get();
        return getCustomerResponse(customer);
    }

    private static CustomerResponse getCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getBirthDate(),
                customer.getGender(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getAddress(),
                customer.getPermissions());
    }

    public CustomerPageResponse search(int page, int pageSize, Optional<String> sortField, Optional<String> sortOrder, Optional<String> filterField, Optional<String> filterValueOptional) {
        var direction = Sort.Direction.ASC;
        if (sortOrder.isPresent()) {
            switch (sortOrder.get().toLowerCase()) {
                case "asc" -> {}
                case "desc" -> direction = Sort.Direction.DESC;
                default -> throw new RuntimeException("Smer sortiranja nije prepoznat.");
            }
        }

        var pageRequest = PageRequest.of(page, pageSize, Sort.by(direction, sortField.orElse("id")));
        Page<Customer> customerPage;

        if (filterField.isPresent()) {
            var matcher = ExampleMatcher.matching().withMatcher(filterField.get(), contains());
            if (filterValueOptional.isEmpty())
                throw new RuntimeException("Specificirano polje za filtriranje ali nije specificirana vrednost.");
            var filterValue = filterValueOptional.get();
            var customer = new Customer();
            switch (filterField.get()) {
                case "id" -> customer.setId(Long.valueOf(filterValue));
                case "firstName" -> customer.setFirstName(filterValue);
                case "lastName" -> customer.setLastName(filterValue);
                case "birthDate" -> customer.setBirthDate(Long.valueOf(filterValue));
                case "gender" -> customer.setGender(Gender.valueOf(filterValue.toUpperCase()));
                case "email" -> customer.setEmail(filterValue);
                case "phoneNumber" -> customer.setPhoneNumber(filterValue);
                case "address" -> customer.setAddress(filterValue);
                default -> throw new RuntimeException("Polje za filtriranje nije prepoznato.");
            }
            var example = Example.of(customer, matcher);
            customerPage = customerRepository.findAll(example, pageRequest);
        }
        else
            customerPage = customerRepository.findAll(pageRequest);
        return new CustomerPageResponse(
                customerPage.getTotalElements(),
                customerPage.stream().map(CustomerService::getCustomerResponse).toList()
        );
    }
}
