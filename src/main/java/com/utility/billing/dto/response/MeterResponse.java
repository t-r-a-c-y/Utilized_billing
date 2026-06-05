package com.utility.billing.dto.response;

import com.utility.billing.entity.enums.MeterStatus;
import com.utility.billing.entity.enums.MeterType;

import java.time.LocalDate;

public record MeterResponse(
        Long id,
        String meterNumber,
        MeterType meterType,
        LocalDate installationDate,
        MeterStatus status,
        Long customerId,
        String customerName
) {}
