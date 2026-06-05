package com.utility.billing.dto.request;

import com.utility.billing.entity.enums.MeterStatus;
import com.utility.billing.entity.enums.MeterType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

@Schema(description = "Create / update meter payload")
public record MeterRequest(

        @Schema(example = "MTR-EL-0001")
        @NotBlank String meterNumber,

        @Schema(example = "ELECTRICITY")
        @NotNull MeterType meterType,

        @Schema(example = "2025-01-15", description = "Cannot be a future date")
        @NotNull @PastOrPresent(message = "Installation date cannot be in the future")
        LocalDate installationDate,

        @Schema(example = "1", description = "Optional: leave null to create an UNASSIGNED meter for a customer to claim")
        Long customerId,

        @Schema(example = "ACTIVE", description = "Defaults to ACTIVE when omitted")
        MeterStatus status
) {}
