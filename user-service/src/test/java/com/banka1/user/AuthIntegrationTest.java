package com.banka1.user;

import com.banka1.user.DTO.request.LoginRequest;
import com.banka1.user.utils.ResponseMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/auth";
    }

    @BeforeEach
    void setup() {
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @Test
    void testLoginAndLogoutFlow() {
        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@admin.com");
        loginRequest.setPassword("admin");  // Podaci iz BootstrapData

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(getBaseUrl() + "/login", loginRequest, Map.class);

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode(), "Login nije uspešan.");
        assertNotNull(loginResponse.getBody().get("data"), "Login odgovor ne sadrži data.");

        @SuppressWarnings("unchecked")
        String token = (String) ((Map<String, Object>) loginResponse.getBody().get("data")).get("token");
        assertNotNull(token, "Token nije generisan tokom login-a.");

        // Logout
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<Void> logoutRequest = new HttpEntity<>(headers);

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> logoutResponse = restTemplate.exchange(getBaseUrl() + "/logout", HttpMethod.POST, logoutRequest, Map.class);
        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode(), "Logout nije uspešan.");

        @SuppressWarnings("unchecked")
        String message = (String) ((Map<String, Object>) logoutResponse.getBody().get("data")).get("message");
        assertEquals(ResponseMessage.LOGOUT_SUCCESS.toString(), message, "Poruka nakon logout-a nije ispravna.");
    }

    @Test
    void testLoginAndLogoutWithBlacklistCheck() {
        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@admin.com");
        loginRequest.setPassword("admin");

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(getBaseUrl() + "/login", loginRequest, Map.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());

        @SuppressWarnings("unchecked")
        String token = (String) ((Map<String, Object>) loginResponse.getBody().get("data")).get("token");
        assertNotNull(token);

        // Logout
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<Void> logoutRequest = new HttpEntity<>(headers);

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> logoutResponse = restTemplate.exchange(getBaseUrl() + "/logout", HttpMethod.POST, logoutRequest, Map.class);
        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());

        @SuppressWarnings("unchecked")
        String message = (String) ((Map<String, Object>) logoutResponse.getBody().get("data")).get("message");
        assertEquals(ResponseMessage.LOGOUT_SUCCESS.toString(), message);

        // Pokusaj da koristis isti token nakon logout-a
        HttpEntity<Void> protectedRequest = new HttpEntity<>(headers);

        HttpStatusCodeException exception = assertThrows(HttpStatusCodeException.class, () -> restTemplate.exchange(
            getBaseUrl() + "/refresh-token",
            HttpMethod.POST,
            protectedRequest,
            Map.class)
        );

        // Provera da je token odbijen (jer je blacklistovan)
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals(ResponseMessage.INVALID_LOGIN.toString(), exception.getResponseBodyAs(Map.class).get("error"));
    }
}
