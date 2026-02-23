package com.example.BlogApp.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT Authentication Entry Point for handling unauthorized access (401) responses.
 * <p>
 * This component is invoked when a request attempts to access a protected resource
 * without valid JWT authentication. It returns a structured JSON error response.
 * </p>
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Handles the case when a client requests a protected resource without authentication.
     * Returns a JSON response with error details and HTTP status 401 (Unauthorized).
     *
     * @param request that resulted in an AuthenticationException
     * @param response so that the user agent can begin authentication
     * @param authException that caused the invocation
     * @throws IOException if an input or output error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        log.error("Unauthorized access attempt. Error: {}", authException.getMessage());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String errorResponse = String.format(
                "{\"success\":false,\"status\":%d,\"message\":\"Unauthorized: %s\",\"error\":\"Authentication Required\",\"path\":\"%s\"}",
                HttpServletResponse.SC_UNAUTHORIZED,
                authException.getMessage(),
                request.getServletPath()
        );

        response.getWriter().write(errorResponse);
    }
}


