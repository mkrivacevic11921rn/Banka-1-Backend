package com.banka1.user.integration;

import com.banka1.user.model.Employee;
import com.banka1.user.model.helper.Department;
import com.banka1.user.model.helper.Gender;
import com.banka1.common.model.Permission;
import com.banka1.common.model.Position;
import com.banka1.user.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @WithMockUser(username = "admin", authorities = {"CREATE_EMPLOYEE"})
    void testCreateEmployeeIntegration() throws Exception {
        String jsonPayload = """
        {
            "firstName": "Petar",
            "lastName": "Petrovic",
            "birthDate": "1990-05-15",
            "gender": "MALE",
            "email": "petar@example.com",
            "phoneNumber": "+381641234567",
            "address": "Knez Mihailova 10, Beograd",
            "username": "petar90",
            "position": "MANAGER",
            "department": "HR",
            "active": true,
            "isAdmin": false,
            "permissions": ["READ_EMPLOYEE", "CREATE_EMPLOYEE"]
        }
    """;

        mockMvc.perform(post("/api/users/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        Employee employee = employeeRepository.findByEmail("petar@example.com").orElse(null);
        assertNotNull(employee);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"CREATE_EMPLOYEE"})
    void testUpdateEmployeeIntegration() throws Exception {
        Employee employee = new Employee();
        employee.setEmail("update@example.com");
        employee.setFirstName("OldName");
        employee.setLastName("Petrovic");
        employee.setAddress("Knez Mihailova 10");
        employee.setActive(true);
        employee.setPermissions(List.of(Permission.CREATE_EMPLOYEE));
        employee.setBirthDate("1990-05-15");
        employee.setPosition(Position.MANAGER);
        employee.setDepartment(Department.ACCOUNTING);
        employee.setIsAdmin(false);
        employee.setPhoneNumber("+381641234567");
        employee.setGender(Gender.FEMALE);
        employee.setUsername("ppetrovic123");
        employeeRepository.save(employee);

        String jsonPayload = """
        {
            "firstName": "NewName"
        }
        """;

        mockMvc.perform(put("/api/users/employee/" + employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"CREATE_EMPLOYEE"})
    void testDeleteEmployeeIntegration() throws Exception {
        Employee employee = new Employee();
        employee.setEmail("delete@example.com");
        employee.setFirstName("OldName");
        employee.setLastName("Petrovic");
        employee.setAddress("Knez Mihailova 10");
        employee.setActive(true);
        employee.setPermissions(List.of(Permission.CREATE_EMPLOYEE));
        employee.setBirthDate("1990-05-15");
        employee.setPosition(Position.MANAGER);
        employee.setDepartment(Department.ACCOUNTING);
        employee.setIsAdmin(false);
        employee.setPhoneNumber("+381641234567");
        employee.setGender(Gender.FEMALE);
        employee.setUsername("ppetrovic123");
        employeeRepository.save(employee);

        mockMvc.perform(delete("/api/users/employee/" + employee.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"CREATE_EMPLOYEE"})
    void testUpdatePermissionsIntegration() throws Exception {
        Employee employee = new Employee();
        employee.setEmail("permissions@example.com");
        employee.setFirstName("OldName");
        employee.setLastName("Petrovic");
        employee.setAddress("Knez Mihailova 10");
        employee.setActive(true);
        employee.setPermissions(List.of(Permission.CREATE_EMPLOYEE));
        employee.setBirthDate("1990-05-15");
        employee.setPosition(Position.MANAGER);
        employee.setDepartment(Department.ACCOUNTING);
        employee.setIsAdmin(false);
        employee.setPhoneNumber("+381641234567");
        employee.setGender(Gender.FEMALE);
        employee.setUsername("ppetrovic123");
        employeeRepository.save(employee);

        String jsonPayload = """
        {
            "permissions": ["READ_EMPLOYEE"]
        }
        """;

        mockMvc.perform(put("/api/users/employee/" + employee.getId() + "/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
