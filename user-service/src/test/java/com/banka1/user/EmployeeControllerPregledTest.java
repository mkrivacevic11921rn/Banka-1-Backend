package com.banka1.user;

import com.banka1.user.DTO.response.EmployeeResponse;
import com.banka1.user.controllers.EmployeeController;
import com.banka1.user.model.helper.Department;
import com.banka1.user.model.helper.Gender;
import com.banka1.user.model.helper.Position;
import com.banka1.user.service.EmployeeService;
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

public class EmployeeControllerPregledTest {
    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    void findByIdSuccess() throws Exception {
        String id = "1";
        var response = new EmployeeResponse(
                1L,
                "Petar",
                "Petrovic",
                "ppetrovic",
                "9999-09-09",
                Gender.MALE,
                "ppetrovic@banka.rs",
                "99999999",
                "Ulica",
                Position.WORKER,
                Department.IT,
                true,
                false,
                List.of());

        Mockito.when(employeeService.findById(id)).thenReturn(response);

        var responseContent = mockMvc.perform(MockMvcRequestBuilders.get("/api/users/employees/" + id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(id))
                .andReturn().getResponse().getContentAsString();
        var responseJson = objectMapper.convertValue(objectMapper.readTree(responseContent).get("data"), EmployeeResponse.class);

        AssertionErrors.assertEquals("Response", response, responseJson);

        Mockito.verify(employeeService).findById(id);
    }

    @Test
    void findByIdNotFound() throws Exception {
        String id = "1";

        Mockito.when(employeeService.findById(id)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employee/" + id))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));

        Mockito.verify(employeeService).findById(id);
    }

    @Test
    void findByIdBadRequest() throws Exception {
        String id = "1";

        Mockito.when(employeeService.findById(id)).thenThrow(new RuntimeException("Poruka"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employee/" + id))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));

        Mockito.verify(employeeService).findById(id);
    }
}
