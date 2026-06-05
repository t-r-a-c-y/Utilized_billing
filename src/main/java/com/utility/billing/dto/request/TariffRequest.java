package com.utility.billing.dto.request;

import com.utility.billing.entity.enums.MeterType;
import com.utility.billing.entity.enums.TariffType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Configure a new tariff version. Supersedes the previous active version for the same meter type.")
public record TariffRequest(

        @Schema(example = "Electricity Residential 2026")
        @NotBlank String name,

        @Schema(example = "ELECTRICITY")
        @NotNull MeterType meterType,

        @Schema(example = "TIERED")
        @NotNull TariffType tariffType,

        @Schema(example = "89.0000", description = "Required when tariffType = FLAT")
        BigDecimal ratePerUnit,

        @Schema(example = "1500.00")
        @NotNull @PositiveOrZero BigDecimal serviceCharge,

        @Schema(example = "2026-07-01", description = "Must be in the future to only affect future billing cycles")
        @NotNull LocalDate effectiveStart,

        @Schema(description = "Required when tariffType = TIERED")
        @Valid List<TariffTierRequest> tiers
) {}
