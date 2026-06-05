package com.utility.billing.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Returned by flows that issue an OTP (signup, login step 1, forgot-password).
 * The caller must submit the emailed code to the matching verify endpoint.
 */
@Schema(description = "Acknowledgement that an OTP has been emailed")
public record OtpChallengeResponse(
        @Schema(example = "alice@utility.rw") String email,
        @Schema(example = "LOGIN") String purpose,
        @Schema(example = "true") boolean otpRequired,
        @Schema(example = "10") int expiresInMinutes,
        @Schema(example = "An OTP has been sent to your email. Submit it to /api/v1/auth/verify-otp.")
        String message
) {}
