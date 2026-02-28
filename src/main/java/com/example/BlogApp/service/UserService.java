package com.example.BlogApp.service;

import com.example.BlogApp.model.User;
import com.example.BlogApp.repo.UserRepo;
import com.example.BlogApp.security.JwtTokenProvider;
import com.example.BlogApp.security.MyUserDetailsService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    public void saveUser(@NonNull User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepo.save(user);
    }

    public List<String> findAll() {
        return userRepo.findAll().stream().map(User::getUsername).toList();
    }

    public String varifyUser(@NonNull User user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );
        UserDetails userDetails = myUserDetailsService.loadUserByUsername(user.getUsername());
        return authentication.isAuthenticated() ? jwtTokenProvider.generateToken(userDetails) : "Authentication Failed";
    }
}
