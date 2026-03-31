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

    public String verifyUser(@NonNull LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        return authentication.isAuthenticated() ? generateTokenForUser(loginRequest.getUsername()) : "Authentication Failed";
    }

    /**
     * Generate JWT token for a given user
     * Used when registering a new user to immediately provide authentication
     *
     * @param username the username to generate token for
     * @return JWT token string
     */
    public String generateTokenForUser(@NonNull String username) {
        UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
        return jwtTokenProvider.generateToken(userDetails);
    }
}
