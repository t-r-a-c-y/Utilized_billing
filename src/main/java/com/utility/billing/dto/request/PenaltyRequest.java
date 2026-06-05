package com.utility.billing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Configure a new late-payment penalty version")
public record PenaltyRequest(

        @Schema(example = "Late payment penalty")
        @NotBlank String name,

        @Schema(example = "5.00")
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal percentage,

        @Schema(example = "2026-07-01")
        @NotNull LocalDate effectiveStart
) {}
