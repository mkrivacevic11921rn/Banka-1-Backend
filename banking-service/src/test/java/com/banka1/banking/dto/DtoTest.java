package com.banka1.banking.dto;

import com.banka1.banking.dto.request.CreateAccountDTO;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DtoTest {
    
    @Test
    public void testAllDtos() {
        // Test AuthorizedPersonDTO
        AuthorizedPersonDTO authorizedPersonDTO = new AuthorizedPersonDTO();
        authorizedPersonDTO.setFirstName("John");
        authorizedPersonDTO.setLastName("Doe");
        authorizedPersonDTO.setBirthDate(946684800000L); // 2000-01-01
        authorizedPersonDTO.setPhoneNumber("+1234567890");
        authorizedPersonDTO.setCompanyID(123L);
        
        assertEquals("John", authorizedPersonDTO.getFirstName());
        assertEquals("Doe", authorizedPersonDTO.getLastName());
        assertEquals(946684800000L, authorizedPersonDTO.getBirthDate());
        assertEquals("+1234567890", authorizedPersonDTO.getPhoneNumber());
        assertEquals(123L, authorizedPersonDTO.getCompanyID());
        
        // Test hashCode and equals for AuthorizedPersonDTO
        AuthorizedPersonDTO sameAuthorizedPerson = new AuthorizedPersonDTO();
        sameAuthorizedPerson.setFirstName("John");
        sameAuthorizedPerson.setLastName("Doe");
        sameAuthorizedPerson.setBirthDate(946684800000L);
        sameAuthorizedPerson.setPhoneNumber("+1234567890");
        sameAuthorizedPerson.setCompanyID(123L);
        
        AuthorizedPersonDTO differentAuthorizedPerson = new AuthorizedPersonDTO();
        differentAuthorizedPerson.setFirstName("Jane");

        // Test CreateAccountByEmployeeDTO
        CreateAccountDTO createAccountDTO = new CreateAccountDTO();
        CreateAccountByEmployeeDTO createAccountByEmployeeDTO = new CreateAccountByEmployeeDTO(createAccountDTO, 456L);
        CreateAccountByEmployeeDTO emptyCreateAccountByEmployeeDTO = new CreateAccountByEmployeeDTO();
        
        emptyCreateAccountByEmployeeDTO.setCreateAccountDTO(createAccountDTO);
        emptyCreateAccountByEmployeeDTO.setEmployeeId(456L);
        
        assertEquals(createAccountDTO, createAccountByEmployeeDTO.getCreateAccountDTO());
        assertEquals(456L, createAccountByEmployeeDTO.getEmployeeId());
        assertEquals(createAccountByEmployeeDTO, emptyCreateAccountByEmployeeDTO);
        assertNotEquals(createAccountByEmployeeDTO, new CreateAccountByEmployeeDTO(createAccountDTO, 789L));
        assertEquals(createAccountByEmployeeDTO.hashCode(), emptyCreateAccountByEmployeeDTO.hashCode());
        
        // Test CustomerDTO
        CustomerDTO customerDTO = new CustomerDTO(1L, "Alice", "Smith", "1985-05-15", 
                "alice@example.com", "+9876543210", "123 Main St");
        CustomerDTO emptyCustomerDTO = new CustomerDTO();
        emptyCustomerDTO.setId(1L);
        emptyCustomerDTO.setFirstName("Alice");
        emptyCustomerDTO.setLastName("Smith");
        emptyCustomerDTO.setBirthDate("1985-05-15");
        emptyCustomerDTO.setEmail("alice@example.com");
        emptyCustomerDTO.setPhoneNumber("+9876543210");
        emptyCustomerDTO.setAddress("123 Main St");
        
        List<String> permissions = Arrays.asList("READ", "WRITE");
        customerDTO.setPermissions(permissions);
        emptyCustomerDTO.setPermissions(permissions);
        
        assertEquals(1L, customerDTO.getId());
        assertEquals("Alice", customerDTO.getFirstName());
        assertEquals("Smith", customerDTO.getLastName());
        assertEquals("1985-05-15", customerDTO.getBirthDate());
        assertEquals("alice@example.com", customerDTO.getEmail());
        assertEquals("+9876543210", customerDTO.getPhoneNumber());
        assertEquals("123 Main St", customerDTO.getAddress());
        assertEquals(permissions, customerDTO.getPermissions());
        assertEquals(customerDTO, emptyCustomerDTO);
        assertNotEquals(customerDTO, new CustomerDTO(2L, "Alice", "Smith", "1985-05-15", 
                "alice@example.com", "+9876543210", "123 Main St"));
        
        // Test NotificationDTO
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setEmail("user@example.com");
        notificationDTO.setSubject("Test Subject");
        notificationDTO.setMessage("Test Message");
        notificationDTO.setFirstName("Bob");
        notificationDTO.setLastName("Brown");
        notificationDTO.setType("INFO");
        
        assertEquals("user@example.com", notificationDTO.getEmail());
        assertEquals("Test Subject", notificationDTO.getSubject());
        assertEquals("Test Message", notificationDTO.getMessage());
        assertEquals("Bob", notificationDTO.getFirstName());
        assertEquals("Brown", notificationDTO.getLastName());
        assertEquals("INFO", notificationDTO.getType());
        
        NotificationDTO sameNotification = new NotificationDTO();
        sameNotification.setEmail("user@example.com");
        sameNotification.setSubject("Test Subject");
        sameNotification.setMessage("Test Message");
        sameNotification.setFirstName("Bob");
        sameNotification.setLastName("Brown");
        sameNotification.setType("INFO");

        
        // Test ExchangePreviewDTO
        ExchangePreviewDTO exchangePreviewDTO = new ExchangePreviewDTO("USD", "EUR", 100.0);
        ExchangePreviewDTO emptyExchangePreviewDTO = new ExchangePreviewDTO();
        emptyExchangePreviewDTO.setFromCurrency("USD");
        emptyExchangePreviewDTO.setToCurrency("EUR");
        emptyExchangePreviewDTO.setAmount(100.0);
        
        assertEquals("USD", exchangePreviewDTO.getFromCurrency());
        assertEquals("EUR", exchangePreviewDTO.getToCurrency());
        assertEquals(100.0, exchangePreviewDTO.getAmount());
        assertEquals(exchangePreviewDTO, emptyExchangePreviewDTO);
        assertNotEquals(exchangePreviewDTO, new ExchangePreviewDTO("USD", "EUR", 200.0));
        assertEquals(exchangePreviewDTO.hashCode(), emptyExchangePreviewDTO.hashCode());
        
        // Test toString methods
        assertNotNull(authorizedPersonDTO.toString());
        assertNotNull(createAccountByEmployeeDTO.toString());
        assertNotNull(customerDTO.toString());
        assertNotNull(notificationDTO.toString());
        assertNotNull(exchangePreviewDTO.toString());
    }
}