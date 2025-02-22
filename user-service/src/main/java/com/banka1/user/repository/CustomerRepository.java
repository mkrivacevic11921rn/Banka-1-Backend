package com.banka1.user.repository;

import com.banka1.user.model.Customer;
import com.banka1.user.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
