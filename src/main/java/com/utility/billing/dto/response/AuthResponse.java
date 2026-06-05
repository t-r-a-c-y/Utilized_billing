package com.utility.billing.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT authentication response")
public record AuthResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiJ9...") String token,
        @Schema(example = "Bearer") String tokenType,
        @Schema(example = "admin@utility.rw") String email,
        @Schema(example = "ROLE_ADMIN") String role,
        @Schema(example = "86400000") long expiresInMs
) {
    public static AuthResponse of(String token, String email, String role, long expiresInMs) {
        return new AuthResponse(token, "Bearer", email, role, expiresInMs);
    }
}
