package com.example.BlogApp.DTO.userDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object for User
 * <p>
 * Used for API responses to avoid exposing sensitive user data.
 * Does not include password or other sensitive information.
 * </p>
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;
}
