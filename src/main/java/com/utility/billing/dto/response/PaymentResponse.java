package com.utility.billing.dto.response;

import com.utility.billing.entity.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
        Long id,
        String billReference,
        BigDecimal amountPaid,
        PaymentMethod paymentMethod,
        LocalDate paymentDate,
        String transactionReference,
        BigDecimal billOutstandingBalance,
        String billStatus
) {}
