package com.utility.billing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Submit an OTP code to verify an account or complete login")
public record OtpVerificationRequest(

        @Schema(example = "alice@utility.rw")
        @NotBlank @Email String email,

        @Schema(example = "482915")
        @NotBlank String otp
) {}
