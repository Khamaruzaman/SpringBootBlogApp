package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.AuthResponse;
import com.example.BlogApp.DTO.authDTO.JwtAuthenticationResponse;
import com.example.BlogApp.DTO.authDTO.LoginRequest;
import com.example.BlogApp.DTO.authDTO.RegisterRequest;
import com.example.BlogApp.model.User;
import com.example.BlogApp.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Operations related to authentication")
public class AuthenticationController {

    private AuthenticationService authenticationService;

    /**
     * Register a new user
     *
     * @param registerRequest the registration details
     * @return AuthResponse containing user data and JWT token
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user and returns a JWT token upon successful registration")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid registration details")
    public ResponseEntity<AuthResponse<JwtAuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        User savedUser = authenticationService.saveUser(registerRequest);

        // Generate JWT token for the newly registered user
        JwtAuthenticationResponse jwtResponse = JwtAuthenticationResponse.builder()
                .accessToken(authenticationService.generateTokenForUser(savedUser.getUsername()))
                .username(savedUser.getUsername())
                .tokenType("Bearer")
                .build();

        AuthResponse<JwtAuthenticationResponse> response = AuthResponse.<JwtAuthenticationResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(jwtResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user with username and password
     *
     * @param loginRequest the login credentials
     * @return AuthResponse containing JWT token and user information
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user and returns a JWT token upon successful login")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid username or password")
    public ResponseEntity<AuthResponse<JwtAuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        String token = authenticationService.verifyUser(loginRequest);

        JwtAuthenticationResponse jwtResponse = JwtAuthenticationResponse.builder()
                .accessToken(token)
                .username(loginRequest.getUsername())
                .tokenType("Bearer")
                .build();

        AuthResponse<JwtAuthenticationResponse> response = AuthResponse.<JwtAuthenticationResponse>builder()
                .success(true)
                .message("Login successful")
                .data(jwtResponse)
                .build();

        return ResponseEntity.ok(response);
    }
}
