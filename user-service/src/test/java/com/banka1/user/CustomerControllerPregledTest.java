package com.banka1.user;

import com.banka1.user.DTO.response.CustomerResponse;
import com.banka1.user.controllers.CustomerController;
import com.banka1.user.model.helper.Gender;
import com.banka1.user.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.AssertionErrors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

public class CustomerControllerPregledTest {
    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
    }

    @Test
    void findByIdSuccess() throws Exception {
        String id = "1";
        var response = new CustomerResponse(
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

        Mockito.when(customerService.findById(id)).thenReturn(response);

        var responseContent = mockMvc.perform(MockMvcRequestBuilders.get("/api/customer/" + id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(id))
                .andReturn().getResponse().getContentAsString();
        var responseJson = objectMapper.convertValue(objectMapper.readTree(responseContent).get("data"), CustomerResponse.class);

        AssertionErrors.assertEquals("Response", response, responseJson);

        Mockito.verify(customerService).findById(id);
    }

    @Test
    void findByIdNotFound() throws Exception {
        String id = "1";

        Mockito.when(customerService.findById(id)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/customer/" + id))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));

        Mockito.verify(customerService).findById(id);
    }

    @Test
    void findByIdBadRequest() throws Exception {
        String id = "1";

        Mockito.when(customerService.findById(id)).thenThrow(new RuntimeException("Poruka"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/customer/" + id))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));

        Mockito.verify(customerService).findById(id);
    }
}
