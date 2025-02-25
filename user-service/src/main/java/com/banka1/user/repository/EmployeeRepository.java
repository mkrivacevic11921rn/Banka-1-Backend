package com.banka1.user.repository;

import com.banka1.user.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

     boolean existsByEmail(String email);
     boolean existsById(Long id);
     Employee findByEmail(String email);
}
