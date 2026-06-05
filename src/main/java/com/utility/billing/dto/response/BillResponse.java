package com.utility.billing.dto.response;

import com.utility.billing.entity.enums.BillStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BillResponse(
        Long id,
        String billReference,
        Long customerId,
        String customerName,
        Long meterId,
        String meterNumber,
        Integer month,
        Integer year,
        BigDecimal consumption,
        BigDecimal tariffAmount,
        BigDecimal serviceCharge,
        BigDecimal taxAmount,
        BigDecimal penaltyAmount,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal outstandingBalance,
        LocalDate dueDate,
        BillStatus status
) {}
