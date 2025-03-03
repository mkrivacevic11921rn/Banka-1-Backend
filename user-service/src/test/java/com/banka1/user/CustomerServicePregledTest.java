package com.banka1.user;

import com.banka1.user.DTO.response.CustomerPageResponse;
import com.banka1.user.DTO.response.CustomerResponse;
import com.banka1.user.model.Customer;
import com.banka1.user.model.helper.Gender;
import com.banka1.user.repository.CustomerRepository;
import com.banka1.user.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.AssertionErrors;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;

public class CustomerServicePregledTest {
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByIdSuccess() {
        String id = "1";
        var entity = getCustomer();

        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(entity));

        var response = getCustomerResponse();

        AssertionErrors.assertEquals("Response", response, customerService.findById(id));

        Mockito.verify(customerRepository).findById(1L);
    }

    private static Customer getCustomer() {
        var entity = new Customer();

        entity.setFirstName("Petar");
        entity.setLastName("Petrovic");
        entity.setUsername("ppetrovic");
        entity.setBirthDate(1234567890L);
        entity.setGender(Gender.MALE);
        entity.setEmail("ppetrovic@banka.rs");
        entity.setPhoneNumber("99999999");
        entity.setAddress("Ulica");
        entity.setPermissions(List.of());
        entity.setId(1L);
        return entity;
    }

    private static CustomerResponse getCustomerResponse() {
        return new CustomerResponse(
                1L,
                "Petar",
                "Petrovic",
                "ppetrovic",
                1234567890L,
                Gender.MALE,
                "ppetrovic@banka.rs",
                "99999999",
                "Ulica",
                List.of());
    }

    @Test
    void findByIdNotFound() {
        String id = "1";

        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        AssertionErrors.assertNull("Response", customerService.findById(id));

        Mockito.verify(customerRepository).findById(1L);
    }

    @Test
    void findByIdInvalidId() {
        String id = "Petar";

        try {
            customerService.findById(id);
            fail("No exception.");
        } catch (Exception e) {
            AssertionErrors.assertNotNull("Error", e);
        }
    }

    @Test
    void search() {
        var response = new CustomerPageResponse(1, List.of(getCustomerResponse()));

        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Mockito.when(customerRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(getCustomer()), pageable, 1));

        AssertionErrors.assertEquals("Response", response,
                customerService.search(0, 10, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));

        Mockito.verify(customerRepository).findAll(pageable);
    }

    @Test
    void searchNoFilterValue() {
        try {
            customerService.search(0, 10, Optional.empty(), Optional.empty(), Optional.of("firstName"), Optional.empty());
            fail("No exception.");
        } catch (Exception e) {
            AssertionErrors.assertNotNull("Error", e);
        }
    }
}
