package com.banka1.user;

import com.banka1.user.DTO.response.EmployeeResponse;
import com.banka1.user.DTO.response.EmployeesPageResponse;
import com.banka1.user.model.Employee;
import com.banka1.user.model.helper.Department;
import com.banka1.user.model.helper.Gender;
import com.banka1.user.model.helper.Position;
import com.banka1.user.repository.EmployeeRepository;
import com.banka1.user.service.EmployeeService;
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

public class EmployeeServicePregledTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByIdSuccess() {
        String id = "1";
        var entity = getEmployee();

        Mockito.when(employeeRepository.findById(1L)).thenReturn(Optional.of(entity));

        var response = getEmployeeResponse();

        AssertionErrors.assertEquals("Response", response, employeeService.findById(id));

        Mockito.verify(employeeRepository).findById(1L);
    }

    private static EmployeeResponse getEmployeeResponse() {
        return new EmployeeResponse(
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
    }

    private static Employee getEmployee() {
        var entity = new Employee();

        entity.setFirstName("Petar");
        entity.setLastName("Petrovic");
        entity.setBirthDate("9999-09-09");
        entity.setGender(Gender.MALE);
        entity.setEmail("ppetrovic@banka.rs");
        entity.setPhoneNumber("99999999");
        entity.setAddress("Ulica");
        entity.setPosition(Position.WORKER);
        entity.setDepartment(Department.IT);
        entity.setActive(true);
        entity.setIsAdmin(false);
        entity.setPermissions(List.of());
        entity.setId(1L);
        return entity;
    }

    @Test
    void findByIdNotFound() {
        String id = "1";

        Mockito.when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        AssertionErrors.assertNull("Response", employeeService.findById(id));

        Mockito.verify(employeeRepository).findById(1L);
    }

    @Test
    void findByIdInvalidId() {
        String id = "Petar";

        try {
            employeeService.findById(id);
            fail("No exception.");
        } catch (Exception e) {
            AssertionErrors.assertNotNull("Error", e);
        }
    }

    @Test
    void search() {
        var response = new EmployeesPageResponse(1, List.of(getEmployeeResponse()));

        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Mockito.when(employeeRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(getEmployee()), pageable, 1));

        AssertionErrors.assertEquals("Response", response,
                employeeService.search(0, 10, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));

        Mockito.verify(employeeRepository).findAll(pageable);
    }

    @Test
    void searchNoFilterValue() {
        try {
            employeeService.search(0, 10, Optional.empty(), Optional.empty(), Optional.of("firstName"), Optional.empty());
            fail("No exception.");
        } catch (Exception e) {
            AssertionErrors.assertNotNull("Error", e);
        }
    }
}
