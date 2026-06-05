package com.utility.billing.dto.request;

import com.utility.billing.entity.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Record a payment against a bill")
public record PaymentRequest(

        @Schema(example = "BILL-2026-05-000001")
        @NotBlank String billReference,

        @Schema(example = "10000.00")
        @NotNull @Positive BigDecimal amountPaid,

        @Schema(example = "MOBILE_MONEY")
        @NotNull PaymentMethod paymentMethod,

        @Schema(example = "2026-06-05", description = "Defaults to today when omitted")
        LocalDate paymentDate,

        @Schema(example = "MM-REF-99812")
        String transactionReference
) {}
