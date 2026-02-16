package com.amalitech.notesApi.controller;

import com.amalitech.notesApi.dto.request.AuthRequest;
import com.amalitech.notesApi.dto.response.AuthResponse;
import com.amalitech.notesApi.service.interfaces.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthControllerTest {

    private AuthController authController;
    private UserServiceInterface userService;

    private AuthRequest registerRequest;
    private AuthRequest loginRequest;

    @BeforeEach
    void setup() {
        userService = Mockito.mock(UserServiceInterface.class);
        authController = new AuthController(userService);

        registerRequest = new AuthRequest("user@example.com", "password123");
        loginRequest = new AuthRequest("user@example.com", "password123");
    }

    @Test
    void registerUser_success() {
        // Mock behavior
        Mockito.doNothing().when(userService).createUser(registerRequest);

        ResponseEntity<String> response = authController.register(registerRequest);
        assertEquals("User registered successfully", response.getBody());

        Mockito.verify(userService).createUser(registerRequest);
    }

    @Test
    void loginUser_success() {
        String token = "fake-jwt-token";
        Mockito.when(userService.loginUser(loginRequest))
                .thenReturn(new AuthResponse(token));

        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        assertEquals(token, response.getBody().token());

        Mockito.verify(userService).loginUser(loginRequest);
    }

    @Test
    void registerUser_throwsWhenUserExists() {
        Mockito.doThrow(new RuntimeException("User exists"))
                .when(userService).createUser(registerRequest);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authController.register(registerRequest));

        assertEquals("User exists", ex.getMessage());
    }

    @Test
    void loginUser_throwsWhenInvalidCredentials() {
        Mockito.when(userService.loginUser(loginRequest))
                .thenThrow(new IllegalArgumentException("Invalid credentials"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authController.login(loginRequest));

        assertEquals("Invalid credentials", ex.getMessage());
    }
}
