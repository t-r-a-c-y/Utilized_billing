package com.utility.billing.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error envelope returned by the global exception handler.
 */
@Schema(description = "Standard error response")
public record ApiError(
        @Schema(example = "2026-06-05T10:15:30") LocalDateTime timestamp,
        @Schema(example = "409") int status,
        @Schema(example = "Conflict") String error,
        @Schema(example = "Customer already exists with National ID: 1199080012345678") String message,
        @Schema(example = "/api/v1/customers") String path,
        @Schema(description = "Field-level validation errors, if any") Map<String, String> fieldErrors
) {
    public ApiError(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    public ApiError(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this(LocalDateTime.now(), status, error, message, path, fieldErrors);
    }
}
