package com.banka1.user.controllers;


import com.banka1.user.DTO.CustomerDTO.CustomerDTO;
import com.banka1.user.model.Customer;
import com.banka1.user.model.helper.Permission;
import com.banka1.user.repository.CustomerRepository;
import com.banka1.user.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
    }

    @Test
    void testCreateCustomer() throws Exception {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setIme("Petar");
        customerDTO.setPrezime("Petrovic");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Petar");
        customer.setLastName("Petrovic");

        when(customerService.createCustomer(any(CustomerDTO.class))).thenReturn(customer);

        mockMvc.perform(post("/api/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    void testUpdateCustomer() throws Exception {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setIme("Petar");
        customerDTO.setPrezime("Petrovic");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Petar");
        customer.setLastName("Petrovic");

        when(customerService.updateCustomer(eq(1L), any(CustomerDTO.class))).thenReturn(Optional.of(customer));

        mockMvc.perform(put("/api/customer/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Podaci korisnika ažurirani"));
    }

    @Test
    void testDeleteCustomer() throws Exception {
        when(customerService.deleteCustomer(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Korisnik uspešno obrisan"));
    }

//    @Test
//    void testDeleteCustomerNotFound() throws Exception {
//        when(customerService.deleteCustomer(1L)).thenReturn(false);
//
//        mockMvc.perform(delete("/api/customer/1")
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.data").value("Korisnik nije pronađen"));
//
//        verify(customerService).deleteCustomer(1L);
//    }

    @Test
    void testUpdatePermissions() throws Exception {
        List<Permission> permissions = List.of(Permission.READ_EMPLOYEE);

        Customer customer = new Customer();
        customer.setId(1L);

        when(customerService.updateCustomerPermissions(eq(1L), anyList())).thenReturn(Optional.of(customer));

        mockMvc.perform(put("/api/customer/1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("permissions", permissions))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Permisije ažurirane"));
    }

    @Test
    void testUpdateCustomerPermissionsNotFound() throws Exception {
        List<Permission> permissions = List.of(Permission.READ_EMPLOYEE);

        when(customerService.updateCustomerPermissions(eq(1L), anyList())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/customer/" + 1L + "/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("permissions", permissions))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Korisnik nije pronađen"));
    }
}
