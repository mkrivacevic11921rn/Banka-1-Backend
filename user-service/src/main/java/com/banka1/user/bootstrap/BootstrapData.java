package com.banka1.user.bootstrap;

import com.banka1.user.model.Customer;
import com.banka1.user.model.Employee;
import com.banka1.common.model.Department;
import com.banka1.user.model.helper.Gender;
import com.banka1.common.model.Permission;
import com.banka1.common.model.Position;
import com.banka1.user.repository.CustomerRepository;
import com.banka1.user.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BootstrapData implements CommandLineRunner {
    public final EmployeeRepository employeeRepository;
    public final CustomerRepository customerRepository;

    @Autowired
    public BootstrapData(EmployeeRepository employeeRepository, CustomerRepository customerRepository) {
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("============== Loading Data ==============");

        Employee admin = new Employee();

        String rawPassword = "admin123";
        String salt = "salt";
        String hashedPassword = BCrypt.hashpw(rawPassword + salt, BCrypt.gensalt());

        admin.setFirstName("Admin");
        admin.setLastName("Admin");
        admin.setEmail("admin@admin.com");
        admin.setPassword(hashedPassword);
        admin.setIsAdmin(true);
        admin.setPhoneNumber("1234567890");
        admin.setBirthDate("2000-01-01");
        admin.setGender(Gender.MALE);
        admin.setDepartment(Department.HR);
        admin.setPosition(Position.DIRECTOR);
        admin.setActive(true);
        admin.setAddress("Admin Address");
        admin.setSaltPassword(salt);
        admin.setUsername("admin123");

        admin.setPermissions(List.of(Permission.values()));

        employeeRepository.save(admin);

        Employee employee1 = new Employee();
        String rawPassword1 = "Per@12345";
        String salt1 = "salt1";
        String hashedPassword1 = BCrypt.hashpw(rawPassword1 + salt1, BCrypt.gensalt());

        employee1.setFirstName("Petar");
        employee1.setLastName("Petrović");
        employee1.setEmail("petar.petrovic@banka.com");
        employee1.setUsername("perica");
        employee1.setPhoneNumber("+381641001000");
        employee1.setBirthDate("1990-07-07");
        employee1.setAddress("Knez Mihailova 5");
        employee1.setGender(Gender.MALE);
        employee1.setPosition(Position.MANAGER);
        employee1.setDepartment(Department.IT);
        employee1.setActive(true);
        employee1.setIsAdmin(false);
        employee1.setPermissions(List.of(Permission.CREATE_CUSTOMER,Permission.DELETE_CUSTOMER,Permission.LIST_CUSTOMER,Permission.EDIT_CUSTOMER,Permission.READ_CUSTOMER,Permission.SET_CUSTOMER_PERMISSION,Permission.SET_EMPLOYEE_PERMISSION,Permission.DELETE_EMPLOYEE,Permission.EDIT_EMPLOYEE,Permission.LIST_EMPLOYEE,Permission.READ_EMPLOYEE,Permission.CREATE_EMPLOYEE));
        employee1.setSaltPassword(salt1);
        employee1.setPassword(hashedPassword1);

        employeeRepository.save(employee1);

        Employee employee2 = new Employee();
        String rawPassword2 = "Jovan@12345";
        String salt2 = "salt2";
        String hashedPassword2 = BCrypt.hashpw(rawPassword2 + salt2, BCrypt.gensalt());

        employee2.setFirstName("Jovana");
        employee2.setLastName("Jovanović");
        employee2.setEmail("jovana.jovanovic@banka.com");
        employee2.setUsername("jjovanaa");
        employee2.setPhoneNumber("+381641001001");
        employee2.setBirthDate("2000-10-10");
        employee2.setAddress("Knez Mihailova 6");
        employee2.setGender(Gender.FEMALE);
        employee2.setPosition(Position.WORKER);
        employee2.setDepartment(Department.HR);
        employee2.setActive(true);
        employee2.setIsAdmin(false);
        employee2.setPermissions(List.of(Permission.READ_CUSTOMER,Permission.CREATE_CUSTOMER,Permission.DELETE_CUSTOMER,Permission.LIST_CUSTOMER,Permission.EDIT_CUSTOMER));
        employee2.setSaltPassword(salt2);
        employee2.setPassword(hashedPassword2);

        employeeRepository.save(employee2);

        Employee employee3 = new Employee();

        employee3.setFirstName("Nikolina");
        employee3.setLastName("Jovanović");
        employee3.setEmail("nikolina.jovanovic@banka.com");
        employee3.setUsername("nikolinaaa");
        employee3.setPhoneNumber("+381641001001");
        employee3.setBirthDate("2000-10-10");
        employee3.setAddress("Knez Mihailova 6");
        employee3.setGender(Gender.FEMALE);
        employee3.setPosition(Position.WORKER);
        employee3.setDepartment(Department.SUPERVISOR);
        employee3.setActive(true);
        employee3.setIsAdmin(false);
        employee3.setPermissions(List.of(Permission.READ_CUSTOMER,Permission.CREATE_CUSTOMER,Permission.DELETE_CUSTOMER,Permission.LIST_CUSTOMER,Permission.EDIT_CUSTOMER));
        employee3.setSaltPassword(salt2);
        employee3.setPassword(hashedPassword2);

        employeeRepository.save(employee3);

        Employee employee4 = new Employee();

        employee4.setFirstName("Milica");
        employee4.setLastName("Jovanović");
        employee4.setEmail("milica.jovanovic@banka.com");
        employee4.setUsername("milicaaaa");
        employee4.setPhoneNumber("+381641001001");
        employee4.setBirthDate("2000-10-10");
        employee4.setAddress("Knez Mihailova 6");
        employee4.setGender(Gender.FEMALE);
        employee4.setPosition(Position.WORKER);
        employee4.setDepartment(Department.AGENT);
        employee4.setActive(true);
        employee4.setIsAdmin(false);
        employee4.setPermissions(List.of(Permission.READ_CUSTOMER,Permission.CREATE_CUSTOMER,Permission.DELETE_CUSTOMER,Permission.LIST_CUSTOMER,Permission.EDIT_CUSTOMER));
        employee4.setSaltPassword(salt2);
        employee4.setPassword(hashedPassword2);

        employeeRepository.save(employee4);

        Customer customer1 = new Customer();
        String rawPassword3 = "M@rko12345";
        String salt3 = "salt3";
        String hashedPassword3 = BCrypt.hashpw(rawPassword3 + salt3, BCrypt.gensalt());

        customer1.setFirstName("Marko");
        customer1.setLastName("Marković");
        customer1.setEmail("marko.markovic@banka.com");
        customer1.setUsername("okram");
        customer1.setPhoneNumber("+381641001002");
        customer1.setBirthDate("2005-12-12");
        customer1.setGender(Gender.MALE);
        customer1.setAddress("Knez Mihailova 7");
        customer1.setPermissions(List.of(Permission.READ_EMPLOYEE));
        customer1.setSaltPassword(salt3);
        customer1.setPassword(hashedPassword3);

        customerRepository.save(customer1);

        Customer customer2 = new Customer();
        String rawPassword4 = "Anastas12345";
        String salt4 = "salt4";
        String hashedPassword4 = BCrypt.hashpw(rawPassword4 + salt4, BCrypt.gensalt());

        customer2.setFirstName("Anastasija");
        customer2.setLastName("Milinković");
        customer2.setEmail("anastasija.milinkovic@banka.com");
        customer2.setUsername("anastass");
        customer2.setPhoneNumber("+381641001003");
        customer2.setBirthDate("2001-02-02");
        customer2.setGender(Gender.FEMALE);
        customer2.setAddress("Knez Mihailova 8");
        customer2.setPermissions(List.of(Permission.READ_EMPLOYEE));
        customer2.setSaltPassword(salt4);
        customer2.setPassword(hashedPassword4);

        customerRepository.save(customer2);

        Customer customer3 = new Customer();
        String rawPassword5 = "Jov@njovan1";
        String salt5 = "salt5";
        String hashedPassword5 = BCrypt.hashpw(rawPassword5 + salt5, BCrypt.gensalt());

        customer3.setFirstName("Jovan");
        customer3.setLastName("Pavlovic");
        customer3.setEmail("jpavlovic6521rn@raf.rs");
        customer3.setUsername("jovan");
        customer3.setPhoneNumber("+381641001003");
        customer3.setBirthDate("2001-02-02");
        customer3.setGender(Gender.MALE);
        customer3.setAddress("Knez Mihailova 8");
        customer3.setPermissions(List.of(Permission.READ_EMPLOYEE));
        customer3.setSaltPassword(salt5);
        customer3.setPassword(hashedPassword5);

        customerRepository.save(customer3);

        Customer customer4 = new Customer();
        String rawPassword6 = "Nemanjanemanj@1";
        String salt6 = "salt6";
        String hashedPassword6 = BCrypt.hashpw(rawPassword6 + salt6, BCrypt.gensalt());

        customer4.setFirstName("Nemanja");
        customer4.setLastName("Marjanov");
        customer4.setEmail("nmarjanov6121rn@raf.rs");
        customer4.setUsername("nemanja");
        customer4.setPhoneNumber("+381641001123");
        customer4.setBirthDate("2001-02-02");
        customer4.setGender(Gender.MALE);
        customer4.setAddress("Knez Mihailova 8");
        customer4.setPermissions(List.of(Permission.READ_EMPLOYEE));
        customer4.setSaltPassword(salt6);
        customer4.setPassword(hashedPassword6);

        customerRepository.save(customer4);

        Customer customer5 = new Customer();
        String rawPassword7 = "Nikola12345";
        String salt7 = "salt7";
        String hashedPassword7 = BCrypt.hashpw(rawPassword7 + salt7, BCrypt.gensalt());

        customer5.setFirstName("Nikola");
        customer5.setLastName("Nikolic");
        customer5.setEmail("primer@primer.rs");
        customer5.setUsername("nikkola");
        customer5.setPhoneNumber("+381641001303");
        customer5.setBirthDate("2001-02-02");
        customer5.setGender(Gender.MALE);
        customer5.setAddress("Knez Mihailova 8");
        customer5.setPermissions(List.of(Permission.READ_EMPLOYEE));
        customer5.setSaltPassword(salt7);
        customer5.setPassword(hashedPassword7);

        customerRepository.save(customer5);

        Customer customer6 = new Customer();
        String rawPassword8 = "nemanjanemanja";
        String salt8 = "salt8";
        String hashedPassword8 = BCrypt.hashpw(rawPassword8 + salt8, BCrypt.gensalt());

        customer6.setFirstName("Jelena");
        customer6.setLastName("Jovanovic");
        customer6.setEmail("jelena@primer.rs");
        customer6.setUsername("jelena");
        customer6.setPhoneNumber("+381621001003");
        customer6.setBirthDate("2001-02-02");
        customer6.setGender(Gender.FEMALE);
        customer6.setAddress("Knez Mihailova 8");
        customer6.setPermissions(List.of(Permission.READ_EMPLOYEE));
        customer6.setSaltPassword(salt8);
        customer6.setPassword(hashedPassword8);

        customerRepository.save(customer6);


        System.out.println("============== Data Loaded ==============");
    }
}
