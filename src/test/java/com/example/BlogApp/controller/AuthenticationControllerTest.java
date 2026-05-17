package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.authDTO.LoginRequest;
import com.example.BlogApp.DTO.authDTO.RegisterRequest;
import com.example.BlogApp.exception.AuthenticationException;
import com.example.BlogApp.exception.GlobalExceptionHandler;
import com.example.BlogApp.model.User;
import com.example.BlogApp.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.example.BlogApp.utils.TestUtil.asJsonString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        try (@SuppressWarnings("unused") var autoCloseable = MockitoAnnotations.openMocks(this)) {
            mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .build();
        }
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Test successful user registration")
        void testRegisterSuccess() throws Exception {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("testuser")
                    .email("test@gmail.com")
                    .password("Test@123456")
                    .confirmPassword("Test@123456")
                    .build();

            User mockedUser = User.builder()
                    .username("testuser")
                    .email("test@gmail.com")
                    .build();

            when(authenticationService.saveUser(any(RegisterRequest.class))).thenReturn(mockedUser);
            when(authenticationService.generateTokenForUser(mockedUser.getUsername())).thenReturn("mocked-jwt-token");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User registered successfully"))
                    .andExpect(jsonPath("$.data.accessToken").value("mocked-jwt-token"))
                    .andExpect(jsonPath("$.data.username").value(mockedUser.getUsername()))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

            verify(authenticationService, times(1)).saveUser(any(RegisterRequest.class));
            verify(authenticationService, times(1)).generateTokenForUser(anyString());
        }

        @Test
        @DisplayName("Test registration with mismatched passwords")
        void testRegisterWithMismatchedPasswords() throws Exception {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("testuser")
                    .email("test@gmail.com")
                    .password("Test@123456")
                    .confirmPassword("Test@654321")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("The input data is invalid"));

            verify(authenticationService, never()).saveUser(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("Test registration with weak password")
        void testRegisterWithWeakPassword() throws Exception {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("testuser")
                    .email("test@gmail.com")
                    .password("weak123")
                    .confirmPassword("weak123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("The input data is invalid"));

            verify(authenticationService, never()).saveUser(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("Test registration with invalid username - too short")
        void testRegisterWithInvalidUsernameShort() throws Exception {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("usr")
                    .email("test@gmail.com")
                    .password("Test@123456")
                    .confirmPassword("Test@123456")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("The input data is invalid"));

            verify(authenticationService, never()).saveUser(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("Test registration with invalid username - special characters")
        void testRegisterWithInvalidUsernameSpecialChars() throws Exception {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("test@user!")
                    .email("test@gmail.com")
                    .password("Test@123456")
                    .confirmPassword("Test@123456")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("The input data is invalid"));

            verify(authenticationService, never()).saveUser(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("Test registration with invalid email format")
        void testRegisterWithInvalidEmail() throws Exception {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("testuser")
                    .email("invalid-email")
                    .password("Test@123456")
                    .confirmPassword("Test@123456")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("The input data is invalid"));

            verify(authenticationService, never()).saveUser(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("Test registration with empty username")
        void testRegisterWithEmptyUsername() throws Exception {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("")
                    .email("test@gmail.com")
                    .password("Test@123456")
                    .confirmPassword("Test@123456")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("The input data is invalid"));

            verify(authenticationService, never()).saveUser(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("Test registration with empty password")
        void testRegisterWithEmptyPassword() throws Exception {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("testuser")
                    .email("test@gmail.com")
                    .password("")
                    .confirmPassword("")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(authenticationService, never()).saveUser(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("Test registration with duplicate username")
        void testRegisterWithDuplicateUsername() throws Exception {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("existinguser")
                    .email("newemail@gmail.com")
                    .password("Test@123456")
                    .confirmPassword("Test@123456")
                    .build();

            when(authenticationService.saveUser(any(RegisterRequest.class)))
                    .thenThrow(new org.springframework.dao.DuplicateKeyException("Username already exists"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Username or email already exists"));

            verify(authenticationService, times(1)).saveUser(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("Test registration with duplicate email")
        void testRegisterWithDuplicateEmail() throws Exception {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("newuser")
                    .email("existing@gmail.com")
                    .password("Test@123456")
                    .confirmPassword("Test@123456")
                    .build();

            when(authenticationService.saveUser(any(RegisterRequest.class)))
                    .thenThrow(new org.springframework.dao.DuplicateKeyException("Email already exists"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Username or email already exists"));

            verify(authenticationService, times(1)).saveUser(any(RegisterRequest.class));
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Test successful user login")
        void testLoginSuccess() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("testuser")
                    .password("Test@123456")
                    .build();

            when(authenticationService.verifyUser(any(LoginRequest.class))).thenReturn("mocked-jwt-token");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Login successful"))
                    .andExpect(jsonPath("$.data.accessToken").value("mocked-jwt-token"))
                    .andExpect(jsonPath("$.data.username").value(loginRequest.getUsername()))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

            verify(authenticationService, times(1)).verifyUser(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Test login with invalid credentials")
        void testLoginWithInvalidCredentials() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("testuser")
                    .password("WrongPassword123")
                    .build();

            when(authenticationService.verifyUser(any(LoginRequest.class)))
                    .thenThrow(new AuthenticationException("Invalid username or password"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid username or password"));

            verify(authenticationService, times(1)).verifyUser(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Test login with non-existent user")
        void testLoginWithNonExistentUser() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("nonexistentuser")
                    .password("Test@123456")
                    .build();

            when(authenticationService.verifyUser(any(LoginRequest.class)))
                    .thenThrow(new AuthenticationException("Invalid username or password"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));

            verify(authenticationService, times(1)).verifyUser(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Test login with invalid username format")
        void testLoginWithInvalidUsernameFormat() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("usr")
                    .password("Test@123456")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("The input data is invalid"));

            verify(authenticationService, never()).verifyUser(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Test login with invalid password format - too short")
        void testLoginWithInvalidPasswordFormat() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("testuser")
                    .password("short")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("The input data is invalid"));

            verify(authenticationService, never()).verifyUser(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Test login with empty username")
        void testLoginWithEmptyUsername() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("")
                    .password("Test@123456")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(authenticationService, never()).verifyUser(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Test login with empty password")
        void testLoginWithEmptyPassword() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("testuser")
                    .password("")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(authenticationService, never()).verifyUser(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Test login with username containing special characters")
        void testLoginWithSpecialCharactersInUsername() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("test@user!")
                    .password("Test@123456")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(asJsonString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("The input data is invalid"));

            verify(authenticationService, never()).verifyUser(any(LoginRequest.class));
        }
    }
}
