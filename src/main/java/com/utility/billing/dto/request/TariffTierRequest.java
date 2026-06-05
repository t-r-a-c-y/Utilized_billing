package com.utility.billing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "A single tier/block of a tiered tariff")
public record TariffTierRequest(

        @Schema(example = "20.00", description = "Inclusive upper bound of units; null means the top open-ended tier")
        BigDecimal upToUnit,

        @Schema(example = "320.0000")
        @NotNull @Positive BigDecimal ratePerUnit
) {}
