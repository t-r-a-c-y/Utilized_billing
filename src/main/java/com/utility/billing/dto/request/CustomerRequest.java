package com.utility.billing.dto.request;

import com.utility.billing.entity.enums.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Create / update customer payload")
public record CustomerRequest(

        @Schema(example = "John Habimana")
        @NotBlank String fullNames,

        @Schema(example = "1199080012345678")
        @NotBlank @Size(min = 8, max = 32) String nationalId,

        @Schema(example = "john@example.rw")
        @NotBlank @Email String email,

        @Schema(example = "+250788654321")
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$") String phoneNumber,

        @Schema(example = "KG 11 Ave, Kigali")
        @NotBlank String address,

        @Schema(example = "ACTIVE", description = "Defaults to ACTIVE when omitted")
        CustomerStatus status
) {}
