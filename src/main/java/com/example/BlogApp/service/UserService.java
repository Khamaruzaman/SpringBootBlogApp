package com.example.BlogApp.service;

import com.example.BlogApp.model.User;
import com.example.BlogApp.repo.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepo userRepo;

    public List<String> findAll() {
        return userRepo.findAll().stream().map(User::getUsername).toList();
    }
}
