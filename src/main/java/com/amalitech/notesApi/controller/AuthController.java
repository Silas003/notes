package com.amalitech.notesApi.controller;

import com.amalitech.notesApi.dto.request.AuthRequest;
import com.amalitech.notesApi.dto.response.AuthResponse;
import com.amalitech.notesApi.models.User;
import com.amalitech.notesApi.security.JwtFilter;
import com.amalitech.notesApi.security.JwtUtil;
import com.amalitech.notesApi.service.UserService;
import com.amalitech.notesApi.service.interfaces.UserServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final UserServiceInterface userService;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {

        userService.createUser(request);
        return ResponseEntity.ok().body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }
}
