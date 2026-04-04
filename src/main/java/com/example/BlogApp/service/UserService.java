package com.example.BlogApp.service;

import com.example.BlogApp.DTO.userDTO.UpdateUserRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.exception.ResourceNotFoundException;
import com.example.BlogApp.model.User;
import com.example.BlogApp.repo.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepo userRepo;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserDTO getUserById(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToUserDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
        return mapToUserDTO(user);
    }

    public UserDTO updateUserProfile(UUID userId, UpdateUserRequest request) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check uniqueness for username if changed
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepo.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(request.getUsername());
        }

        // Check uniqueness for email if changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepo.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        }

        user.setUpdatedAt(Instant.now());
        userRepo.save(user);
        return mapToUserDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepo.findAll().stream()
                .map(this::mapToUserDTO)
                .toList();
    }

    public void deleteUser(UUID userId) {
        if (!userRepo.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepo.deleteById(userId);
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
