package com.example.BlogApp.security;

import com.example.BlogApp.exception.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

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

    private final HandlerExceptionResolver resolver;

    // Inject the primary HandlerExceptionResolver
    public JwtAuthenticationEntryPoint(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Handles the case when a client requests a protected resource without authentication.
     * Returns a JSON response with error details and HTTP status 401 (Unauthorized) from {@link GlobalExceptionHandler}.
     *
     * @param request that resulted in an AuthenticationException
     * @param response so that the user agent can begin authentication
     * @param authException that caused the invocation
     */
    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            AuthenticationException authException
    ) {
        log.error("Unauthorized access attempt. Error: {}", authException.getMessage());

        resolver.resolveException(request, response, null, authException);
    }
}


