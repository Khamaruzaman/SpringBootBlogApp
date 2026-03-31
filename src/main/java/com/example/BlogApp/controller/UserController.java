package com.example.BlogApp.controller;

import com.example.BlogApp.service.UserService;
import lombok.AllArgsConstructor;
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
    public List<String> getUsers() {
        return userService.findAll();
    }

}
