package com.utility.billing.dto.request;

import com.utility.billing.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * Registration payload. Customers self-register (role = ROLE_CUSTOMER) and must
 * also supply nationalId + address so a Customer profile is created and linked.
 * Staff roles (OPERATOR/FINANCE) do not need those fields.
 */
@Schema(description = "New account registration payload")
public record SignupRequest(

        @Schema(example = "Alice Mukamana")
        @NotBlank(message = "Full names are required")
        @Size(max = 120, message = "Full names must be at most 120 characters")
        String fullNames,

        @Schema(example = "alice@example.com")
        @NotBlank @Email(message = "A valid email is required")
        String email,

        @Schema(example = "+250788123456")
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number is invalid")
        String phoneNumber,

        // Strong password policy: >=8 chars, with upper, lower, digit and special char.
        @Schema(example = "Secret123!", description = "Min 8 chars incl. upper, lower, number and special character")
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                message = "Password must be at least 8 characters and include an uppercase letter, "
                        + "a lowercase letter, a number and a special character")
        String password,

        @Schema(example = "ROLE_CUSTOMER")
        @NotNull(message = "Role is required")
        Role role,

        @Schema(example = "1199080012345678", description = "Required when role = ROLE_CUSTOMER")
        String nationalId,

        @Schema(example = "KG 11 Ave, Kigali", description = "Required when role = ROLE_CUSTOMER")
        String address
) {}
