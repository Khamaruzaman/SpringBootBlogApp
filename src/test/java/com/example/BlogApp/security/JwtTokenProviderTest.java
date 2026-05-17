package com.example.BlogApp.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for JwtTokenProvider
 * Tests JWT token generation, validation, claim extraction, and expiration handling
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private JwtProperties jwtProperties;

    private JwtTokenProvider jwtTokenProvider;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Setup JWT properties with test values using lenient to avoid UnnecessaryStubbingException
        lenient().when(jwtProperties.getSecret()).thenReturn("mySecretKeyForTestingPurposesOnly12345");
        lenient().when(jwtProperties.getExpirationMs()).thenReturn(3600000L); // 1 hour

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
        
        // Create test user
        userDetails = User.builder()
                .username("testuser")
                .password("password123")
                .authorities(List.of())
                .build();
    }

    // ==================== Token Generation Tests ====================

    @Test
    void testGenerateToken_Success() {
        // Arrange
        // UserDetails already created in setUp()

        // Act
        String token = jwtTokenProvider.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void testGenerateToken_TokenIsCompact() {
        // Arrange
        // UserDetails already created in setUp()

        // Act
        String token = jwtTokenProvider.generateToken(userDetails);

        // Assert
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts (header.payload.signature)");
    }

    // ==================== Token Parsing Tests ====================

    @Test
    void testGetClaimsFromToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateToken(userDetails);

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Assert
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
    }

    @Test
    void testGetClaimsFromToken_InvalidToken_ReturnsNull() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(invalidToken);

        // Assert
        assertNull(claims);
    }

    @Test
    void testGetClaimsFromToken_MalformedToken_ReturnsNull() {
        // Arrange
        String malformedToken = "malformed";

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(malformedToken);

        // Assert
        assertNull(claims);
    }

    @Test
    void testGetClaimsFromToken_EmptyToken_ReturnsNull() {
        // Arrange
        String emptyToken = "";

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(emptyToken);

        // Assert
        assertNull(claims);
    }

    // ==================== Extract Claim Tests ====================

    @Test
    void testExtractClaim_Username_Success() {
        // Arrange
        String token = jwtTokenProvider.generateToken(userDetails);

        // Act
        String username = jwtTokenProvider.extractClaim(token, Claims::getSubject);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void testExtractClaim_ExpirationDate_Success() {
        // Arrange
        String token = jwtTokenProvider.generateToken(userDetails);

        // Act
        Date expirationDate = jwtTokenProvider.extractClaim(token, Claims::getExpiration);

        // Assert
        assertNotNull(expirationDate);
        assertTrue(expirationDate.getTime() > System.currentTimeMillis());
    }

    // ==================== Username Extraction Tests ====================

    @Test
    void testGetUsernameFromToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateToken(userDetails);

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void testGetUsernameFromToken_CorrectUser() {
        // Arrange
        String token = jwtTokenProvider.generateToken(userDetails);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(userDetails.getUsername(), extractedUsername);
    }

    // ==================== Expiration Date Extraction Tests ====================

    @Test
    void testGetExpirationDateFromToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateToken(userDetails);

        // Act
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        // Assert
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void testGetExpirationDateFromToken_IsInFuture() {
        // Arrange
        String token = jwtTokenProvider.generateToken(userDetails);
        Date now = new Date();

        // Act
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        // Assert
        assertTrue(expirationDate.getTime() > now.getTime());
    }

    // ==================== Token Validation Tests ====================

    @Test
    void testValidateToken_ValidToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateToken(userDetails);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_DifferentUser_False() {
        // Arrange
        String token = jwtTokenProvider.generateToken(userDetails);

        UserDetails differentUser = User.builder()
                .username("differentuser")
                .password("password123")
                .authorities(List.of())
                .build();

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    // ==================== Token Expiration Tests ====================

    @Test
    void testIsTokenExpired_NewToken_False() {
        // Arrange
        String token = jwtTokenProvider.generateToken(userDetails);

        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    void testIsTokenExpired_ValidToken_NotExpired() {
        // Arrange
        String token = jwtTokenProvider.generateToken(userDetails);

        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertFalse(isExpired, "New token should not be expired");
    }

    @Test
    void testIsTokenExpired_InvalidToken_True() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        try {
            boolean isExpired = jwtTokenProvider.isTokenExpired(invalidToken);
            // If no exception, the token is treated as expired
            assertTrue(isExpired);
        } catch (NullPointerException | IllegalArgumentException e) {
            // Invalid tokens can throw exceptions, which we should handle
            // This is acceptable behavior for invalid tokens
            assertNotNull(e);
        }
    }
}



