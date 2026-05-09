package com.example.BlogApp.DTO.authDTO;

import com.example.BlogApp.utils.fieldValidators.FieldsValueMatch;
import com.example.BlogApp.utils.fieldValidators.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldsValueMatch(field = "password", fieldMatch = "confirmPassword", message = "Passwords do not match!")
@Schema(description = "Register request payload containing username and password")
public class RegisterRequest {
    @NotBlank(message = "Username cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9_]{5,15}$",
             message = "Username must be 5-15 characters and can only contain letters, numbers, and underscores")
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @StrongPassword
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;
}
