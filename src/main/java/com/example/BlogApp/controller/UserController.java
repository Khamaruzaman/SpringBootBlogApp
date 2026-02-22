package com.example.BlogApp.controller;

import com.example.BlogApp.model.User;
import com.example.BlogApp.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
public class UserController {
    UserService userService;

    @GetMapping("/register")
    public List<String> getUsers() {
        return userService.findAll();
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        userService.saveUser(user);
        return user;
    }
}
