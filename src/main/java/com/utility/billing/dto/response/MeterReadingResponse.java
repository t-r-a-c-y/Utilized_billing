package com.utility.billing.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MeterReadingResponse(
        Long id,
        Long meterId,
        String meterNumber,
        BigDecimal previousReading,
        BigDecimal currentReading,
        BigDecimal consumption,
        LocalDate readingDate,
        Integer month,
        Integer year,
        boolean billed
) {}
