package com.utility.billing.dto.response;

import com.utility.billing.entity.enums.MeterType;
import com.utility.billing.entity.enums.TariffType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TariffResponse(
        Long id,
        String name,
        MeterType meterType,
        TariffType tariffType,
        Integer version,
        BigDecimal ratePerUnit,
        BigDecimal serviceCharge,
        LocalDate effectiveStart,
        LocalDate effectiveEnd,
        List<TariffTierResponse> tiers
) {
    public record TariffTierResponse(Long id, BigDecimal upToUnit, BigDecimal ratePerUnit) {}
}
