package com.utility.billing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login credentials")
public record LoginRequest(

        @Schema(example = "admin@utility.rw")
        @NotBlank @Email
        String email,

        @Schema(example = "Admin123!")
        @NotBlank
        String password
) {}
