package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.AuthResponse;
import com.example.BlogApp.DTO.authDTO.JwtAuthenticationResponse;
import com.example.BlogApp.DTO.authDTO.LoginRequest;
import com.example.BlogApp.DTO.authDTO.RegisterRequest;
import com.example.BlogApp.model.User;
import com.example.BlogApp.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Authentication Operations
 * <p>
 * Handles user registration, login, and JWT token management.
 * All responses are wrapped in {@link AuthResponse} for consistent API format.
 * </p>
 */
@Log4j2
@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private AuthenticationService authenticationService;

    /**
     * Register a new user
     *
     * @param registerRequest the registration details
     * @return AuthResponse containing user data and JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse<JwtAuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        User savedUser = authenticationService.saveUser(registerRequest);

        // Generate JWT token for the newly registered user
        JwtAuthenticationResponse jwtResponse = new JwtAuthenticationResponse();
        jwtResponse.setAccessToken(authenticationService.generateTokenForUser(savedUser.getUsername()));
        jwtResponse.setUsername(savedUser.getUsername());
        jwtResponse.setTokenType("Bearer");

        AuthResponse<JwtAuthenticationResponse> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("User registered successfully");
        response.setData(jwtResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user with username and password
     *
     * @param loginRequest the login credentials
     * @return AuthResponse containing JWT token and user information
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse<JwtAuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        String token = authenticationService.verifyUser(loginRequest);

        JwtAuthenticationResponse jwtResponse = new JwtAuthenticationResponse();
        jwtResponse.setAccessToken(token);
        jwtResponse.setUsername(loginRequest.getUsername());
        jwtResponse.setTokenType("Bearer");

        AuthResponse<JwtAuthenticationResponse> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("Login successful");
        response.setData(jwtResponse);

        return ResponseEntity.ok(response);
    }
}
