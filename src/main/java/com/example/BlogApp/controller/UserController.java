package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.AuthResponse;
import com.example.BlogApp.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {
    UserService userService;

    @GetMapping
    public ResponseEntity<AuthResponse<List<String>>> getUsers() {
        try {
            AuthResponse<List<String>> response = new AuthResponse<>();
            response.setSuccess(true);
            response.setMessage("Users retrieved successfully");
            response.setData(userService.findAll());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            AuthResponse<List<String>> response = new AuthResponse<>();
            response.setSuccess(false);
            response.setMessage("Users retrieved failed: " + e.getMessage());
            response.setData(null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

}
