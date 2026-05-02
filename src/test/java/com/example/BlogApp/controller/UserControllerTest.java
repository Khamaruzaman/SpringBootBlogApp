package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.userDTO.UpdateUserRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.exception.GlobalExceptionHandler;
import com.example.BlogApp.exception.ResourceNotFoundException;
import com.example.BlogApp.service.UserService;
import com.example.BlogApp.utils.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Test")
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UUID userID;
    private Instant randInstant;

    private UserDTO testUserDTO;
    @BeforeEach
    void setup() throws Exception
    {
        try (@SuppressWarnings("unused") var autoCloseable = MockitoAnnotations.openMocks(this)) {
            mockMvc = MockMvcBuilders.standaloneSetup(userController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .build();
        }

        userID = UUID.randomUUID();
        randInstant = Instant.parse("2024-01-01T00:00:00Z");

        testUserDTO = UserDTO.builder()
                .id(userID)
                .username("testUser")
                .email("test@gmail.com")
                .createdAt(randInstant)
                .updatedAt(Instant.now())
                .build();
    }

    // ==================== GET /api/users ====================

    @Test
    @DisplayName("Get All Users - Success")
    void testGetAllUsersSuccess() throws Exception {
        List<UserDTO> userList = List.of(testUserDTO);
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(userID.toString()))
                .andExpect(jsonPath("$.data[0].username").value("testUser"))
                .andExpect(jsonPath("$.data[0].email").value("test@gmail.com"))
                .andExpect(jsonPath("$.data[0].createdAt").value(randInstant.toString()))
                .andExpect(jsonPath("$.data[0].updatedAt").value(randInstant.toString()));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Get All Users - Empty List")
    void testGetAllUsersEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Get All Users - Service Exception")
    void testGetAllUsersServiceException() throws Exception {
        when(userService.getAllUsers()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

        verify(userService, times(1)).getAllUsers();
    }

    // ==================== GET /api/users/{userId} ====================

    @Test
    @DisplayName("Get User by ID - Success")
    void testGetUserByIdSuccess() throws Exception {
        when(userService.getUserById(userID)).thenReturn(testUserDTO);

        mockMvc.perform(get("/api/users/" + userID)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(userID.toString()))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.email").value("test@gmail.com"));

        verify(userService, times(1)).getUserById(userID);
    }

    @Test
    @DisplayName("Get User by ID - Not Found")
    void testGetUserByIdNotFound() throws Exception {
        when(userService.getUserById(userID))
                .thenThrow(new ResourceNotFoundException("User not found with id: " + userID));

        mockMvc.perform(get("/api/users/" + userID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, times(1)).getUserById(userID);
    }

    @Test
    @DisplayName("Get User by ID - Invalid UUID Format")
    void testGetUserByIdInvalidUUID() throws Exception {
        mockMvc.perform(get("/api/users/invalid-uuid-format")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).getUserById(any());
    }

    // ==================== GET /api/users/username/{username} ====================

    @Test
    @DisplayName("Get User by Username - Success")
    void testGetUserByUsernameSuccess() throws Exception {
        String username = "testUser";
        when(userService.getUserByUsername(username)).thenReturn(testUserDTO);

        mockMvc.perform(get("/api/users/username/" + username)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.email").value("test@gmail.com"));

        verify(userService, times(1)).getUserByUsername(username);
    }

    @Test
    @DisplayName("Get User by Username - Not Found")
    void testGetUserByUsernameNotFound() throws Exception {
        String username = "nonExistentUser";
        when(userService.getUserByUsername(username))
                .thenThrow(new ResourceNotFoundException("User not found with username: " + username));

        mockMvc.perform(get("/api/users/username/" + username)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, times(1)).getUserByUsername(username);
    }

    // ==================== PUT /api/users/{userId} ====================

    @Test
    @DisplayName("Update User Profile - Success")
    void testUpdateUserProfileSuccess() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("newUser1")
                .email("newemail@gmail.com")
                .password("newPassword123")
                .confirmPassword("newPassword123")
                .build();

        UserDTO updatedUserDTO = UserDTO.builder()
                .id(userID)
                .username("newUser1")
                .email("newemail@gmail.com")
                .createdAt(randInstant)
                .updatedAt(Instant.now())
                .build();

        when(userService.updateUserProfile(any(UUID.class), any(UpdateUserRequest.class))).thenReturn(updatedUserDTO);

        mockMvc.perform(put("/api/users/" + userID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User profile updated successfully"))
                .andExpect(jsonPath("$.data.username").value("newUser1"))
                .andExpect(jsonPath("$.data.email").value("newemail@gmail.com"));

        verify(userService, times(1)).updateUserProfile(any(UUID.class), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("Update User Profile - Invalid Username Format (Too Short)")
    void testUpdateUserProfileInvalidUsernameShort() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("ab")  // Too short (less than 5 characters)
                .email("test@gmail.com")
                .password("password123")
                .confirmPassword("password123")
                .build();

        mockMvc.perform(put("/api/users/" + userID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    @DisplayName("Update User Profile - Invalid Username Format (Special Characters)")
    void testUpdateUserProfileInvalidUsernameSpecialChars() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("user@name")  // Contains @ which is not allowed
                .email("test@gmail.com")
                .password("password123")
                .confirmPassword("password123")
                .build();

        mockMvc.perform(put("/api/users/" + userID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    @DisplayName("Update User Profile - Invalid Email Format")
    void testUpdateUserProfileInvalidEmail() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("validUser")
                .email("invalid-email-format")  // Invalid email
                .password("password123")
                .confirmPassword("password123")
                .build();

        mockMvc.perform(put("/api/users/" + userID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    @DisplayName("Update User Profile - Short Password")
    void testUpdateUserProfileShortPassword() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("validUser")
                .email("test@gmail.com")
                .password("short")  // Less than 8 characters
                .confirmPassword("short")
                .build();

        mockMvc.perform(put("/api/users/" + userID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    @DisplayName("Update User Profile - Passwords Don't Match")
    void testUpdateUserProfilePasswordsMismatch() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("validUser")
                .email("test@gmail.com")
                .password("password123")
                .confirmPassword("differentPassword")
                .build();

        mockMvc.perform(put("/api/users/" + userID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, never()).updateUserProfile(any(), any());
    }

    @Test
    @DisplayName("Update User Profile - Duplicate Username")
    void testUpdateUserProfileDuplicateUsername() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("existingUser")  // Already exists
                .email("test@gmail.com")
                .password("password123")
                .confirmPassword("password123")
                .build();

        when(userService.updateUserProfile(any(UUID.class), any(UpdateUserRequest.class)))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        mockMvc.perform(put("/api/users/" + userID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username already exists"));

        verify(userService, times(1)).updateUserProfile(any(UUID.class), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("Update User Profile - Duplicate Email")
    void testUpdateUserProfileDuplicateEmail() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("newUser")
                .email("existing@gmail.com")  // Already exists
                .password("password123")
                .confirmPassword("password123")
                .build();

        when(userService.updateUserProfile(any(UUID.class), any(UpdateUserRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        mockMvc.perform(put("/api/users/" + userID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists"));

        verify(userService, times(1)).updateUserProfile(any(UUID.class), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("Update User Profile - User Not Found")
    void testUpdateUserProfileUserNotFound() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("newUser")
                .email("test@gmail.com")
                .password("password123")
                .confirmPassword("password123")
                .build();

        when(userService.updateUserProfile(any(UUID.class), any(UpdateUserRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found with id: " + userID));

        mockMvc.perform(put("/api/users/" + userID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, times(1)).updateUserProfile(any(UUID.class), any(UpdateUserRequest.class));
    }

    // ==================== DELETE /api/users/{userId} ====================

    @Test
    @DisplayName("Delete User - Success")
    void testDeleteUserSuccess() throws Exception {
        doNothing().when(userService).deleteUser(userID);

        mockMvc.perform(delete("/api/users/" + userID)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deleted successfully"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(userService, times(1)).deleteUser(userID);
    }

    @Test
    @DisplayName("Delete User - Not Found")
    void testDeleteUserNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("User not found with id: " + userID))
                .when(userService).deleteUser(userID);

        mockMvc.perform(delete("/api/users/" + userID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        verify(userService, times(1)).deleteUser(userID);
    }

    @Test
    @DisplayName("Delete User - Invalid UUID Format")
    void testDeleteUserInvalidUUID() throws Exception {
        mockMvc.perform(delete("/api/users/invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).deleteUser(any());
    }
}
