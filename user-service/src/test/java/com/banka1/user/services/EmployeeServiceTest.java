package com.banka1.user.services;

import com.banka1.user.DTO.request.CreateEmployeeDto;
import com.banka1.user.DTO.request.UpdateEmployeeDto;
import com.banka1.user.DTO.request.UpdatePermissionsDto;
import com.banka1.user.listener.MessageHelper;
import com.banka1.user.model.Employee;
import com.banka1.user.model.helper.Gender;
import com.banka1.user.model.helper.Permission;
import com.banka1.user.repository.EmployeeRepository;
import com.banka1.user.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private MessageHelper messageHelper;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Marko");
        employee.setLastName("Markovic");
        employee.setGender(Gender.MALE);
    }

    @Test
    void testCreateEmployee() {
        CreateEmployeeDto dto = new CreateEmployeeDto();
        dto.setFirstName("Marko");
        dto.setLastName("Markovic");

        when(modelMapper.map(any(CreateEmployeeDto.class), eq(Employee.class))).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.createEmployee(dto);

        assertNotNull(result);
        assertEquals("Marko", result.getFirstName());
        assertEquals("Markovic", result.getLastName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee() {
        UpdateEmployeeDto dto = new UpdateEmployeeDto();
        dto.setFirstName("Marko");
        dto.setLastName("Markovic");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);


        Employee result = employeeService.updateEmployee(1L, dto);

        assertNotNull(result);
        assertEquals("Marko", result.getFirstName());
        assertEquals("Markovic", result.getLastName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }


    @Test
    void testUpdatePermissions() {
        UpdatePermissionsDto dto = new UpdatePermissionsDto();
        dto.setPermissions(List.of(Permission.READ_EMPLOYEE, Permission.CREATE_EMPLOYEE));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);


        Employee result = employeeService.updatePermissions(1L, dto);

        assertNotNull(result);

        String permissionsAsString = result.getPermissions().stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        assertEquals("READ_EMPLOYEE, CREATE_EMPLOYEE", permissionsAsString);

        verify(employeeRepository, times(1)).save(any(Employee.class));
    }


    @Test
    void testDeleteEmployee(){
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        employee.setEmail("other@example.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(1L);

        // Provera da li je metoda delete pozvana tačno jednom
        verify(employeeRepository, times(1)).delete(employee);
    }

    @Test
    void testUpdateEmployee_NotFound() {
        UpdateEmployeeDto dto = new UpdateEmployeeDto();
        dto.setFirstName("Novi");
        dto.setLastName("Naziv");

        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> employeeService.updateEmployee(1L, dto));
    }
}
