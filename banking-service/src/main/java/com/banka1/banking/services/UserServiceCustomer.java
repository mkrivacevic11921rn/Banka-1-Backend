package com.banka1.banking.services;

import com.banka1.banking.dto.CustomerDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class UserServiceCustomer {

    private final RestTemplate restTemplate;

    @Value("${services.user-service.url}")
    private final String userServiceBaseUrl;

    public UserServiceCustomer(RestTemplate restTemplate,@Value("services.user-service.url") String userServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }
    public CustomerDTO getCustomerById(Long customerId) {
        String url = userServiceBaseUrl + "/api/users/customer/" + customerId;

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);

        if (response.getBody() == null || !response.getBody().containsKey("data")) {
            throw new IllegalArgumentException("Korisnik nije pronađen ili API nije vratio očekivani format.");
        }

        Map<String, Object> customerData = (Map<String, Object>) response.getBody().get("data");

        CustomerDTO customer = new CustomerDTO();
        customer.setId(Long.parseLong(customerData.get("id").toString()));
        customer.setFirstName((String) customerData.get("firstName"));
        customer.setLastName((String) customerData.get("lastName"));
        customer.setBirthDate((String) customerData.get("birthDate"));
        customer.setGender((String) customerData.get("gender"));
        customer.setEmail((String) customerData.get("email"));
        customer.setPhoneNumber((String) customerData.get("phoneNumber"));
        customer.setAddress((String) customerData.get("address"));
        customer.setPermissions((List<String>) customerData.get("permissions"));



        return customer;
    }

}
