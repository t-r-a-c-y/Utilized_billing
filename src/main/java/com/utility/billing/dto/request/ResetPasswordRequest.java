package com.utility.billing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Set a new password using a password-reset OTP")
public record ResetPasswordRequest(

        @Schema(example = "alice@utility.rw")
        @NotBlank @Email String email,

        @Schema(example = "482915")
        @NotBlank String otp,

        @Schema(example = "NewSecret123!")
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
        String newPassword
) {}
