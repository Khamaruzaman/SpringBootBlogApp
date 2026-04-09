package com.example.BlogApp.DTO.userDTO;

import com.example.BlogApp.utils.FieldsValueMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldsValueMatch(field = "password", fieldMatch = "confirmPassword", message = "Passwords do not match!")
public class UpdateUserRequest {
    @NotBlank(message = "Username cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9_]{5,15}$",
             message = "Username must be 5-15 characters and can only contain letters, numbers, and underscores")
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Password is required")
    private String confirmPassword;
}
