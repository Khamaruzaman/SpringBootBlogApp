package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.LoginRequest;
import com.example.BlogApp.DTO.RegisterRequest;
import com.example.BlogApp.model.User;
import com.example.BlogApp.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
public class UserController {
    UserService userService;

    @GetMapping("/register")
    public List<String> getUsers() {
        return userService.findAll();
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody RegisterRequest registerRequest) {
        return userService.saveUser(registerRequest);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest loginRequest) {
        return Map.of(
                "token", userService.varifyUser(loginRequest),
                "username", loginRequest.getUsername(),
                "type", "Bearer"
        );
    }
}
