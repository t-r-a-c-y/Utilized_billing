package com.utility.billing.dto.response;

import com.utility.billing.entity.enums.NotificationStatus;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long customerId,
        String customerName,
        Long billId,
        String message,
        NotificationStatus status,
        LocalDateTime createdAt
) {}
