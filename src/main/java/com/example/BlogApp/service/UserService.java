package com.example.BlogApp.service;

import com.example.BlogApp.DTO.userDTO.UpdateUserRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.exception.ResourceNotFoundException;
import com.example.BlogApp.model.User;
import com.example.BlogApp.repo.UserRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private UserRepo userRepo;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserDTO getUserById(UUID userId) {
        try {
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            return mapToUserDTO(user);
        } catch (Exception e) {
            log.error("Error retrieving user by id {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    public UserDTO getUserByUsername(String username) {
        try {
            User user = userRepo.findByUsername(username);
            if (user == null) {
                throw new ResourceNotFoundException("User not found with username: " + username);
            }
            return mapToUserDTO(user);
        } catch (Exception e) {
            log.error("Error retrieving user by username {}: {}", username, e.getMessage());
            throw e;
        }
    }

    public UserDTO updateUserProfile(UUID userId, UpdateUserRequest request) {
        try {
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
        } catch (Exception e) {
            log.error("Error updating user profile for id {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    public List<UserDTO> getAllUsers() {
        try {
            return userRepo.findAll().stream()
                    .map(this::mapToUserDTO)
                    .toList();
        } catch (Exception e) {
            log.error("Error retrieving all users: {}", e.getMessage());
            throw e;
        }
    }

    public void deleteUser(UUID userId) {
        try {
            if (!userRepo.existsById(userId)) {
                throw new ResourceNotFoundException("User not found with id: " + userId);
            }
            userRepo.deleteById(userId);
        } catch (Exception e) {
            log.error("Error deleting user with id {}: {}", userId, e.getMessage());
            throw e;
        }
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
