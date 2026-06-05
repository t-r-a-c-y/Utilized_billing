package com.utility.billing.dto.request;

import com.utility.billing.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "New account registration payload")
public record SignupRequest(

        @Schema(example = "Alice Mukamana")
        @NotBlank(message = "Full names are required")
        String fullNames,

        @Schema(example = "alice@utility.rw")
        @NotBlank @Email(message = "A valid email is required")
        String email,

        @Schema(example = "+250788123456")
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number is invalid")
        String phoneNumber,

        @Schema(example = "Secret123!")
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @Schema(example = "ROLE_CUSTOMER")
        @NotNull(message = "Role is required")
        Role role
) {}
