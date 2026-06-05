package com.utility.billing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Capture a monthly meter reading")
public record MeterReadingRequest(

        @Schema(example = "1")
        @NotNull Long meterId,

        @Schema(example = "1200.00", description = "If omitted, the last recorded reading is used")
        @PositiveOrZero BigDecimal previousReading,

        @Schema(example = "1320.00")
        @NotNull @PositiveOrZero BigDecimal currentReading,

        @Schema(example = "2026-05-31")
        @NotNull LocalDate readingDate,

        @Schema(example = "5")
        @NotNull @Min(1) @Max(12) Integer month,

        @Schema(example = "2026")
        @NotNull @Min(2000) Integer year
) {}
