package com.utility.billing.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TaxResponse(
        Long id,
        String name,
        BigDecimal percentage,
        Integer version,
        LocalDate effectiveStart,
        LocalDate effectiveEnd
) {}
