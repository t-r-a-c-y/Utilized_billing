package com.utility.billing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login credentials")
public record LoginRequest(

        @Schema(example = "youremail@example.com", description = "Your registered email")
        @NotBlank @Email
        String email,

        @Schema(example = "YourPassword123!", description = "Your account password")
        @NotBlank
        String password
) {}
