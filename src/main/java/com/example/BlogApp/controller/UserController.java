package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.AuthResponse;
import com.example.BlogApp.DTO.userDTO.UpdateUserRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.service.UserService;
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
public class UserController {
    UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AuthResponse<List<UserDTO>>> getAllUsers() {
        AuthResponse<List<UserDTO>> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("Users retrieved successfully");
        response.setData(userService.getAllUsers());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or #userId == authentication.principal.user.id")
    public ResponseEntity<AuthResponse<UserDTO>> getUserById(@PathVariable UUID userId) {
        AuthResponse<UserDTO> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("User retrieved successfully");
        response.setData(userService.getUserById(userId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAuthority('ADMIN') or #username == authentication.principal.user.username")
    public ResponseEntity<AuthResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        AuthResponse<UserDTO> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("User retrieved successfully");
        response.setData(userService.getUserByUsername(username));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or #userId == authentication.principal.user.id")
    public ResponseEntity<AuthResponse<UserDTO>> updateUserProfile(@PathVariable UUID userId, @Valid @RequestBody UpdateUserRequest request) {
        AuthResponse<UserDTO> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("User profile updated successfully");
        response.setData(userService.updateUserProfile(userId, request));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or #userId == authentication.principal.user.id")
    public ResponseEntity<AuthResponse<Void>> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        AuthResponse<Void> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("User deleted successfully");
        response.setData(null);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
