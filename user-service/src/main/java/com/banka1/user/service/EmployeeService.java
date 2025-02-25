package com.banka1.user.service;

import com.banka1.user.DTO.request.CreateEmployeeDto;
import com.banka1.user.DTO.request.UpdateEmployeeDto;
import com.banka1.user.DTO.request.UpdatePermissionsDto;
import com.banka1.user.model.Employee;
import com.banka1.user.repository.EmployeeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ModelMapper modelMapper;

    public Employee createEmployee(CreateEmployeeDto createEmployeeDto) {
        // Provera da li već postoji nalog sa istim email-om
        if (employeeRepository.existsByEmail(createEmployeeDto.getEmail())) {
            throw new RuntimeException("Nalog sa ovim email-om već postoji!");
        }
        Employee employee = modelMapper.map(createEmployeeDto, Employee.class);
        employee.setActive(createEmployeeDto.getActive());

        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, UpdateEmployeeDto updateEmployeeDto) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zaposleni nije pronađen"));

//        // Provera da li korisnik ima dozvolu za ažuriranje
//        if (!hasPermissionToEdit(existingEmployee)) {
//            throw new AccessDeniedException("Nemate dozvolu za ažuriranje ovog zaposlenog");
//        }

        // Ažuriranje dozvoljenih polja
        Optional.ofNullable(updateEmployeeDto.getFirstName()).ifPresent(existingEmployee::setFirstName);
        Optional.ofNullable(updateEmployeeDto.getLastName()).ifPresent(existingEmployee::setLastName);
        Optional.ofNullable(updateEmployeeDto.getPhoneNumber()).ifPresent(existingEmployee::setPhoneNumber);
        Optional.ofNullable(updateEmployeeDto.getAddress()).ifPresent(existingEmployee::setAddress);
        Optional.ofNullable(updateEmployeeDto.getPosition()).ifPresent(existingEmployee::setPosition);
        Optional.ofNullable(updateEmployeeDto.getDepartment()).ifPresent(existingEmployee::setDepartment);
        Optional.ofNullable(updateEmployeeDto.getActive()).ifPresent(existingEmployee::setActive);
        Optional.ofNullable(updateEmployeeDto.getIsAdmin()).ifPresent(existingEmployee::setIsAdmin);

        return employeeRepository.save(existingEmployee);
    }

    public Employee updatePermissions(Long id, UpdatePermissionsDto updatePermissionsDto){
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zaposleni nije pronađen"));


        Optional.ofNullable(updatePermissionsDto.getPermissions()).ifPresent(existingEmployee::setPermissions);

        return employeeRepository.save(existingEmployee);
    }
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zaposleni nije pronađen"));

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Ne možeš obrisati samog sebe
        if (employee.getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("Ne možete obrisati sami sebe");
        }

//        // Samo admin može brisati admina
//        if (employee.getPermissions().contains("admin") && !currentUserHasAdminPermission()) {
//            throw new AccessDeniedException("Samo admin može brisati drugog admina");
//        }

        employeeRepository.delete(employee);
    }

    public boolean existsById(Long id) {
        return employeeRepository.existsById(id);
    }

}
