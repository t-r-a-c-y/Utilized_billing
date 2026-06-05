package com.utility.billing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request a password-reset OTP to be emailed")
public record ForgotPasswordRequest(

        @Schema(example = "alice@utility.rw")
        @NotBlank @Email String email
) {}
