package com.example.BlogApp.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Generic Response Wrapper for API Responses")
public class AuthResponse<T> {
    @Schema(description = "Indicates whether the operation was successful")
    private boolean success;
    @Schema(description = "Message providing additional information about the response")
    private String message;
    @Schema(description = "Optional data payload containing the response data")
    private T data;
}
