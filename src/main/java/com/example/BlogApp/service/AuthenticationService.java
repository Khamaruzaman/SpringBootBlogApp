package com.example.BlogApp.service;

import com.example.BlogApp.DTO.authDTO.LoginRequest;
import com.example.BlogApp.DTO.authDTO.RegisterRequest;
import com.example.BlogApp.exception.AuthenticationException;
import com.example.BlogApp.model.User;
import com.example.BlogApp.repo.UserRepo;
import com.example.BlogApp.security.JwtTokenProvider;
import com.example.BlogApp.security.MyUserDetailsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    private UserRepo userRepo;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private MyUserDetailsService myUserDetailsService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public boolean userExists(@NonNull RegisterRequest registerRequest) {
        boolean exists = userRepo.existsByUsername(registerRequest.getUsername()) || userRepo.existsByEmail(registerRequest.getEmail());
        log.info("User existence check for username: {}, email: {} - exists: {}", registerRequest.getUsername(), registerRequest.getEmail(), exists);
        return exists;
    }

    public User saveUser(@NonNull RegisterRequest registerRequest) {
        try {
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(bCryptPasswordEncoder.encode(registerRequest.getPassword()));
            user.setRoles(Set.of("USER"));
            User savedUser = userRepo.save(user);
            log.info("User {} registered successfully", savedUser.getUsername());
            return savedUser;
        } catch (Exception e) {
            log.error("Error saving user: {}", e.getMessage());
            throw e;
        }
    }

    public String verifyUser(@NonNull LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            if (!authentication.isAuthenticated()) {
                throw new AuthenticationException("Invalid username or password");
            }
            log.info("User {} logged in successfully", loginRequest.getUsername());
            return generateTokenForUser(loginRequest.getUsername());
        } catch (BadCredentialsException e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername());
            throw new AuthenticationException("Invalid username or password");
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication: {}", e.getMessage());
            throw new AuthenticationException("Authentication failed due to an unexpected error");
        }
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
        String token = jwtTokenProvider.generateToken(userDetails);
        log.info("JWT token generated for user: {}", username);
        return token;
    }
}
