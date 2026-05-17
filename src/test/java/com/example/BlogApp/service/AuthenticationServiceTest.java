package com.example.BlogApp.service;

import com.example.BlogApp.DTO.authDTO.LoginRequest;
import com.example.BlogApp.DTO.authDTO.RegisterRequest;
import com.example.BlogApp.exception.AuthenticationException;
import com.example.BlogApp.model.User;
import com.example.BlogApp.repo.UserRepo;
import com.example.BlogApp.security.JwtTokenProvider;
import com.example.BlogApp.security.MyUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService
 * Tests user registration, login, token generation, and credential validation
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private MyUserDetailsService myUserDetailsService;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("Password123!");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    // ==================== User Existence Tests ====================

    @Test
    void testUserExists_UserDoesNotExist_ReturnsFalse() {
        // Arrange
        when(userRepo.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepo.existsByEmail(registerRequest.getEmail())).thenReturn(false);

        // Act
        boolean exists = authenticationService.userExists(registerRequest);

        // Assert
        assertFalse(exists);
    }

    @Test
    void testUserExists_UserExistsByUsername_ReturnsTrue() {
        // Arrange
        when(userRepo.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // Act
        boolean exists = authenticationService.userExists(registerRequest);

        // Assert
        assertTrue(exists);
        verify(userRepo, times(1)).existsByUsername(registerRequest.getUsername());
    }

    @Test
    void testUserExists_UserExistsByEmail_ReturnsTrue() {
        // Arrange
        when(userRepo.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepo.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act
        boolean exists = authenticationService.userExists(registerRequest);

        // Assert
        assertTrue(exists);
    }

    // ==================== User Registration Tests ====================

    @Test
    void testSaveUser_Success() {
        // Arrange
        String encodedPassword = "encoded_password_hash";
        when(bCryptPasswordEncoder.encode(registerRequest.getPassword())).thenReturn(encodedPassword);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User savedUser = authenticationService.saveUser(registerRequest);

        // Assert
        assertNotNull(savedUser);
        assertEquals(registerRequest.getUsername(), savedUser.getUsername());
        assertEquals(registerRequest.getEmail(), savedUser.getEmail());
        assertEquals(encodedPassword, savedUser.getPassword());
        assertEquals(Set.of("USER"), savedUser.getRoles());
        verify(bCryptPasswordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void testSaveUser_PasswordIsEncoded() {
        // Arrange
        String plainPassword = registerRequest.getPassword();
        String encodedPassword = "encoded_hash";
        when(bCryptPasswordEncoder.encode(plainPassword)).thenReturn(encodedPassword);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User savedUser = authenticationService.saveUser(registerRequest);

        // Assert
        assertEquals(encodedPassword, savedUser.getPassword());
        assertNotEquals(plainPassword, savedUser.getPassword());
    }

    @Test
    void testSaveUser_UserRoleIsAssigned() {
        // Arrange
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User savedUser = authenticationService.saveUser(registerRequest);

        // Assert
        assertEquals(Set.of("USER"), savedUser.getRoles());
    }

    // ==================== User Verification/Login Tests ====================

    @Test
    void testVerifyUser_Success() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        String expectedToken = "jwt_token_here";

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(loginRequest.getUsername())
                .password(loginRequest.getPassword())
                .authorities(Arrays.asList())
                .build();
        when(myUserDetailsService.loadUserByUsername(loginRequest.getUsername())).thenReturn(userDetails);
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn(expectedToken);

        // Act
        String token = authenticationService.verifyUser(loginRequest);

        // Assert
        assertNotNull(token);
        assertEquals(expectedToken, token);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testVerifyUser_InvalidCredentials_ThrowsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authenticationService.verifyUser(loginRequest));
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testVerifyUser_UnAuthenticatedUser_ThrowsException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authenticationService.verifyUser(loginRequest));
    }

    // ==================== Token Generation Tests ====================

    @Test
    void testGenerateTokenForUser_Success() {
        // Arrange
        String username = "testuser";
        String expectedToken = "jwt_token_string";

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("password")
                .authorities(Arrays.asList())
                .build();

        when(myUserDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn(expectedToken);

        // Act
        String token = authenticationService.generateTokenForUser(username);

        // Assert
        assertNotNull(token);
        assertEquals(expectedToken, token);
        verify(myUserDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtTokenProvider, times(1)).generateToken(userDetails);
    }

    @Test
    void testGenerateTokenForUser_CallsTokenProvider() {
        // Arrange
        String username = "testuser";

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("password")
                .authorities(Arrays.asList())
                .build();

        when(myUserDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn("token");

        // Act
        authenticationService.generateTokenForUser(username);

        // Assert
        verify(jwtTokenProvider, times(1)).generateToken(userDetails);
    }

    @Test
    void testGenerateTokenForUser_UserNotFound_ThrowsException() {
        // Arrange
        String nonExistentUsername = "nonexistent";
        when(myUserDetailsService.loadUserByUsername(nonExistentUsername))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        // Act & Assert
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> authenticationService.generateTokenForUser(nonExistentUsername));
    }
}

