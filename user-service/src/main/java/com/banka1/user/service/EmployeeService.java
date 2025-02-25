package com.banka1.user.service;

import com.banka1.user.DTO.response.EmployeeResponse;
import com.banka1.user.DTO.response.EmployeesPageResponse;
import com.banka1.user.model.Employee;
import com.banka1.user.model.helper.Department;
import com.banka1.user.model.helper.Gender;
import com.banka1.user.model.helper.Position;
import com.banka1.user.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeResponse findById(String id) {
        var employeeOptional = employeeRepository.findById(Long.parseLong(id));
        if (employeeOptional.isEmpty())
            return null;
        var employee = employeeOptional.get();
        return getEmployeeResponse(employee);
    }

    private static EmployeeResponse getEmployeeResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getBirthDate(),
                employee.getGender(),
                employee.getEmail(),
                employee.getPhoneNumber(),
                employee.getAddress(),
                employee.getPosition(),
                employee.getDepartment(),
                employee.getActive(),
                employee.getIsAdmin(),
                employee.getPermissions());
    }

    public EmployeesPageResponse search(int page, int pageSize, Optional<String> sortField, Optional<String> sortOrder, Optional<String> filterField, Optional<String> filterValueOptional) {
        var direction = Sort.Direction.ASC;
        if (sortOrder.isPresent()) {
            switch (sortOrder.get().toLowerCase()) {
                case "asc" -> {}
                case "desc" -> direction = Sort.Direction.DESC;
                default -> throw new RuntimeException("Smer sortiranja nije prepoznat.");
            }
        }

        var pageRequest = PageRequest.of(page, pageSize, Sort.by(direction, sortField.orElse("id")));
        Page<Employee> employeePage;

        if (filterField.isPresent()) {
            var matcher = ExampleMatcher.matching().withMatcher(filterField.get(), contains());
            if (filterValueOptional.isEmpty())
                throw new RuntimeException("Specificirano polje za filtriranje ali nije specificirana vrednost.");
            var filterValue = filterValueOptional.get();
            var employee = new Employee();
            switch (filterField.get()) {
                case "id" -> employee.setId(Long.valueOf(filterValue));
                case "firstName" -> employee.setFirstName(filterValue);
                case "lastName" -> employee.setLastName(filterValue);
                case "birthDate" -> employee.setBirthDate(filterValue);
                case "gender" -> employee.setGender(Gender.valueOf(filterValue.toUpperCase()));
                case "email" -> employee.setEmail(filterValue);
                case "phoneNumber" -> employee.setPhoneNumber(filterValue);
                case "address" -> employee.setAddress(filterValue);
                case "position" -> employee.setPosition(Position.valueOf(filterValue.toUpperCase()));
                case "department" -> employee.setDepartment(Department.valueOf(filterValue.toUpperCase()));
                case "active" -> employee.setActive(Boolean.valueOf(filterValue.toLowerCase()));
                case "isAdmin" -> employee.setIsAdmin(Boolean.valueOf(filterValue.toLowerCase()));
                default -> throw new RuntimeException("Polje za filtriranje nije prepoznato.");
            }
            var example = Example.of(employee, matcher);
            employeePage = employeeRepository.findAll(example, pageRequest);
        }
        else
            employeePage = employeeRepository.findAll(pageRequest);
        return new EmployeesPageResponse(
                employeePage.getTotalElements(),
                employeePage.stream().map(EmployeeService::getEmployeeResponse).toList()
        );
    }
}
