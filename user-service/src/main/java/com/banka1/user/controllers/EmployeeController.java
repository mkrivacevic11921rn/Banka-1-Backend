package com.banka1.user.controllers;

import com.banka1.user.DTO.request.CreateEmployeeDto;
import com.banka1.user.DTO.request.UpdateEmployeeDto;
import com.banka1.user.DTO.request.UpdatePermissionsDto;
import com.banka1.user.aspect.Authorization;
import com.banka1.user.model.Employee;
import com.banka1.user.model.helper.Permission;
import com.banka1.user.model.helper.Position;
import com.banka1.user.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")

@Tag(name = "Employee API", description = "API za upravljanje zaposlenima")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

   @PostMapping("/employees")
   @Authorization(permissions = { Permission.CREATE_EMPLOYEE }, positions = { Position.HR })
   @Operation(summary = "Kreiranje zaposlenog", description = "Dodaje novog zaposlenog u sistem.")
   @ApiResponses({
           @ApiResponse(responseCode = "201", description = "Zaposleni uspešno kreiran"),
           @ApiResponse(responseCode = "403", description = "Nemaš permisije za ovu akciju")
   })
    public ResponseEntity<?> createEmployee(@RequestBody CreateEmployeeDto createEmployeeDto) {
       Employee savedEmployee = null;
        try {
            savedEmployee = employeeService.createEmployee(createEmployeeDto);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("id", savedEmployee.getId());
        data.put("message", "Zaposleni uspešno kreiran");
        response.put("data", data);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/employee/{id}")
    @Authorization(permissions = { Permission.EDIT_EMPLOYEE }, positions = { Position.HR })
    @Operation(summary = "Ažuriranje zaposlenog", description = "Menja podatke zaposlenog na osnovu ID-a.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Podaci uspešno ažurirani"),
            @ApiResponse(responseCode = "404", description = "Korisnik nije pronađen"),
            @ApiResponse(responseCode = "403", description = "Nemaš permisije za ovu akciju")
    })
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody UpdateEmployeeDto updateEmployeeDto) {
        Employee updatedEmployee = null;
        try {
            updatedEmployee = employeeService.updateEmployee(id, updateEmployeeDto);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", "Podaci korisnika ažurirani");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("employee/{id}")
    @Authorization(permissions = { Permission.DELETE_EMPLOYEE }, positions = { Position.HR })
    @Operation(summary = "Brisanje zaposlenog", description = "Briše zaposlenog iz sistema po ID-u.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Korisnik uspešno obrisan"),
            @ApiResponse(responseCode = "404", description = "Korisnik nije pronađen"),
            @ApiResponse(responseCode = "403", description = "Nemaš permisije za ovu akciju")
    })
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        if (!employeeService.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Zaposleni sa ID-em " + id + " nije pronađen.");
        }
        employeeService.deleteEmployee(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", "Korisnik uspešno obrisan");

        return ResponseEntity.ok(response);
    }

    @PutMapping("employee/{id}/permissions")
    @Authorization(permissions = { Permission.SET_EMPLOYEE_PERMISSION }, positions = { Position.HR })
    @Operation(summary = "Ažuriranje permisija zaposlenom", description = "Menja dozvole zaposlenog.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permisije uspešno ažurirane"),
            @ApiResponse(responseCode = "404", description = "Korisnik nije pronađen"),
            @ApiResponse(responseCode = "403", description = "Nemaš permisije za ovu akciju")
    })
    public ResponseEntity<?> updatePermissions(@PathVariable Long id, @RequestBody UpdatePermissionsDto updatePermissionsDto){

        if (!employeeService.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Zaposleni sa ID-em " + id + " nije pronađen.");
        }

       Employee updatedEmployee = employeeService.updatePermissions(id, updatePermissionsDto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", "Permisije korisnika ažurirane");

        return ResponseEntity.ok(response);
    }
}
