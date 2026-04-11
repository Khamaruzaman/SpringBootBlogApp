package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.AuthResponse;
import com.example.BlogApp.DTO.userDTO.UpdateUserRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for managing users")
public class UserController {
    UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get All Users", description = "Retrieve a list of all users. Accessible only by admins.")
    public ResponseEntity<AuthResponse<List<UserDTO>>> getAllUsers() {
        AuthResponse<List<UserDTO>> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("Users retrieved successfully");
        response.setData(userService.getAllUsers());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or #userId == authentication.principal.user.id")
    @Operation(summary = "Get User by ID", description = "Retrieve user details by user ID. Accessible by admins or the user themselves.")
    public ResponseEntity<AuthResponse<UserDTO>> getUserById(@PathVariable UUID userId) {
        AuthResponse<UserDTO> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("User retrieved successfully");
        response.setData(userService.getUserById(userId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAuthority('ADMIN') or #username == authentication.principal.user.username")
    @Operation(summary = "Get User by Username", description = "Retrieve user details by username. Accessible by admins or the user themselves.")
    public ResponseEntity<AuthResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        AuthResponse<UserDTO> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("User retrieved successfully");
        response.setData(userService.getUserByUsername(username));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or #userId == authentication.principal.user.id")
    @Operation(summary = "Update User Profile", description = "Update user profile information. Accessible by admins or the user themselves.")
    public ResponseEntity<AuthResponse<UserDTO>> updateUserProfile(@PathVariable UUID userId, @Valid @RequestBody UpdateUserRequest request) {
        AuthResponse<UserDTO> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("User profile updated successfully");
        response.setData(userService.updateUserProfile(userId, request));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or #userId == authentication.principal.user.id")
    @Operation(summary = "Delete User", description = "Delete a user account. Accessible by admins or the user themselves.")
    public ResponseEntity<AuthResponse<Void>> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        AuthResponse<Void> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("User deleted successfully");
        response.setData(null);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
