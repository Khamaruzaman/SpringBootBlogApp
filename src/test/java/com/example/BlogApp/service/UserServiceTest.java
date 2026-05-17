package com.example.BlogApp.service;

import com.example.BlogApp.DTO.userDTO.UpdateUserRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.exception.ResourceNotFoundException;
import com.example.BlogApp.model.User;
import com.example.BlogApp.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 * Tests user CRUD operations, profile management, and validation
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("hashed_password");
        testUser.setCreatedAt(Instant.now());
        testUser.setUpdatedAt(Instant.now());
    }

    // ==================== Get User Tests ====================

    @Test
    void testGetUserById_Success() {
        // Arrange
        when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.getUserById(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepo, times(1)).findById(testUserId);
    }

    @Test
    void testGetUserById_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepo.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(testUserId));
        verify(userRepo, times(1)).findById(testUserId);
    }

    @Test
    void testGetUserByUsername_Success() {
        // Arrange
        when(userRepo.findByUsername("testuser")).thenReturn(testUser);

        // Act
        UserDTO result = userService.getUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userRepo, times(1)).findByUsername("testuser");
    }

    @Test
    void testGetUserByUsername_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepo.findByUsername("nonexistent")).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByUsername("nonexistent"));
    }

    // ==================== Update User Profile Tests ====================

    @Test
    void testUpdateUserProfile_UpdateUsername_Success() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("newusername");

        when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepo.existsByUsername("newusername")).thenReturn(false);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserDTO result = userService.updateUserProfile(testUserId, request);

        // Assert
        assertNotNull(result);
        assertEquals("newusername", result.getUsername());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUserProfile_UpdateEmail_Success() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("newemail@example.com");

        when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepo.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserDTO result = userService.updateUserProfile(testUserId, request);

        // Assert
        assertNotNull(result);
        assertEquals("newemail@example.com", result.getEmail());
    }

    @Test
    void testUpdateUserProfile_UpdatePassword_Success() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("newpassword123");

        String encodedPassword = "encoded_password_hash";
        when(bCryptPasswordEncoder.encode("newpassword123")).thenReturn(encodedPassword);
        when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserDTO result = userService.updateUserProfile(testUserId, request);

        // Assert
        assertNotNull(result);
        verify(bCryptPasswordEncoder, times(1)).encode("newpassword123");
    }

    @Test
    void testUpdateUserProfile_DuplicateUsername_ThrowsException() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("existinguser");

        when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepo.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.updateUserProfile(testUserId, request));
    }

    @Test
    void testUpdateUserProfile_DuplicateEmail_ThrowsException() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("existing@example.com");

        when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepo.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.updateUserProfile(testUserId, request));
    }

    @Test
    void testUpdateUserProfile_UserNotFound_ThrowsException() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("newusername");

        when(userRepo.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserProfile(testUserId, request));
    }

    // ==================== Get All Users Tests ====================

    @Test
    void testGetAllUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepo.findAll()).thenReturn(users);

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        verify(userRepo, times(1)).findAll();
    }

    @Test
    void testGetAllUsers_EmptyList() {
        // Arrange
        when(userRepo.findAll()).thenReturn(Arrays.asList());

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetAllUsers_MultipleUsers() {
        // Arrange
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        List<User> users = Arrays.asList(testUser, user2);
        when(userRepo.findAll()).thenReturn(users);

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
    }

    // ==================== Delete User Tests ====================

    @Test
    void testDeleteUser_Success() {
        // Arrange
        when(userRepo.existsById(testUserId)).thenReturn(true);

        // Act
        userService.deleteUser(testUserId);

        // Assert
        verify(userRepo, times(1)).deleteById(testUserId);
    }

    @Test
    void testDeleteUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepo.existsById(testUserId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(testUserId));
    }

    @Test
    void testDeleteUser_VerifyDeleteCalled() {
        // Arrange
        when(userRepo.existsById(testUserId)).thenReturn(true);

        // Act
        userService.deleteUser(testUserId);

        // Assert
        verify(userRepo, times(1)).existsById(testUserId);
        verify(userRepo, times(1)).deleteById(testUserId);
    }
}

