package com.example.BlogApp.Jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT Token Provider for generating, validating, and extracting information from JWT tokens.
 * <p>
 * Responsibilities:
 * - Generate JWT tokens from UserDetails
 * - Validate token signatures and expiration
 * - Extract claims and user information from tokens
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * Generate a compact, signed JWT for the supplied user.
     *
     * The token will contain the user's username as the subject and include
     * issued-at and expiration timestamps. The token is signed using the
     * HMAC secret provided by {@link JwtProperties}.
     *
     * @param userDetails the authenticated user's details whose username will be used as the JWT subject
     * @return a compact JWT string (JWS) containing the subject, issuedAt and expiration claims
     */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpirationMs());

        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Parse and validate a JWT, returning its claims when the token is valid.
     *
     * This method attempts to verify the token's signature using the configured
     * signing key and parse its claims. If parsing or verification fails, the
     * method logs the cause and returns null.
     *
     * @param token the JWT (compact JWS) to parse
     * @return the token's {@link Claims} when parsing and signature verification succeed, or null on failure
     */
    public Claims getClaimsFromToken(String token) {

        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
        } catch (SignatureException ex) {
            log.warn("JWT signature validation failed: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
        } catch (Exception ex) {
            log.warn("JWT validation error: {}", ex.getMessage());
        }
        return null;
    }

    /**
     * Extract a single claim from the token by applying the provided resolver.
     *
     * The resolver function receives the parsed {@link Claims} and should return
     * the desired value (for example, subject, expiration, or a custom claim).
     * Note: if the token cannot be parsed this method may throw a
     * {@code NullPointerException} because {@link #getClaimsFromToken} returns null on failure.
     *
     * @param token the JWT to extract the claim from
     * @param claimsResolver a function that maps {@link Claims} to the desired value
     * @param <T> the type of the extracted claim value
     * @return the value returned by the resolver when applied to the token's claims
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Validate that the token belongs to the provided user and is not expired.
     *
     * The method checks that the token's subject matches the user's username
     * and that the token's expiration has not passed.
     *
     * @param token the JWT to validate
     * @param userDetails the user details to compare against the token's subject
     * @return true if the token's subject equals the user's username and the token is not expired; false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Extract the token subject (username) from the JWT.
     *
     * @param token the JWT containing the subject claim
     * @return the subject (username) stored in the token
     */
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the expiration timestamp from the JWT.
     *
     * @param token the JWT containing the expiration claim
     * @return the {@link Date} when the token expires
     */
    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Check whether the provided JWT has expired.
     *
     * This method reads the token's expiration claim and compares it to the
     * current time. If the token parsing raises {@link ExpiredJwtException},
     * the method treats the token as expired and returns true.
     *
     * @param token the JWT to check
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = getExpirationDateFromToken(token);
            return expirationDate.before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

    /**
     * Decode the base64-encoded secret from configuration and create the HMAC key
     * used for signing and verifying JWTs.
     *
     * @return a {@link SecretKey} derived from the configured base64 secret
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
