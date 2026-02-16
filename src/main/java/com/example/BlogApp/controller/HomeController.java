package com.example.BlogApp.controller;

import com.example.BlogApp.model.User;
import com.example.BlogApp.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HomeController {
    @Autowired
    UserRepo userRepo;

    @GetMapping("/")
    public List<String> home() {
        return userRepo.findAll().stream().map(User::getUsername).toList();
    }
    @PostMapping("/")
    public User homePost(@RequestBody User user) {
        userRepo.save(user);
        return user;
    }
}
