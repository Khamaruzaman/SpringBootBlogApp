package com.example.BlogApp.service;

import com.example.BlogApp.DTO.LoginRequest;
import com.example.BlogApp.DTO.RegisterRequest;
import com.example.BlogApp.model.User;
import com.example.BlogApp.repo.UserRepo;
import com.example.BlogApp.security.JwtTokenProvider;
import com.example.BlogApp.security.MyUserDetailsService;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepo userRepo;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private MyUserDetailsService myUserDetailsService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public User saveUser(@NonNull RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(bCryptPasswordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(Set.of("USER"));
        userRepo.save(user);
        return user;
    }

    public List<String> findAll() {
        return userRepo.findAll().stream().map(User::getUsername).toList();
    }

    public String varifyUser(@NonNull LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        UserDetails userDetails = myUserDetailsService.loadUserByUsername(loginRequest.getUsername());
        return authentication.isAuthenticated() ? jwtTokenProvider.generateToken(userDetails) : "Authentication Failed";
    }
}
