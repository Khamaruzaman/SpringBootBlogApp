package com.example.BlogApp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generic Response Wrapper for API Responses
 * <p>
 * Used to wrap all API responses in a consistent format with status indication,
 * message, and optional data payload.
 * </p>
 *
 * @param <T> the type of data being wrapped
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
