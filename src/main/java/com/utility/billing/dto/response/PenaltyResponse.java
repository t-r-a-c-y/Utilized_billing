package com.utility.billing.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PenaltyResponse(
        Long id,
        String name,
        BigDecimal percentage,
        Integer version,
        LocalDate effectiveStart,
        LocalDate effectiveEnd
) {}
