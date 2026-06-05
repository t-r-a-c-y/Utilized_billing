package com.utility.billing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Set a new password using a password-reset OTP")
public record ResetPasswordRequest(

        @Schema(example = "alice@utility.rw")
        @NotBlank @Email String email,

        @Schema(example = "482915")
        @NotBlank String otp,

        @Schema(example = "NewSecret123!", description = "Min 8 chars incl. upper, lower, number and special character")
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                message = "Password must be at least 8 characters and include an uppercase letter, "
                        + "a lowercase letter, a number and a special character")
        String newPassword
) {}
