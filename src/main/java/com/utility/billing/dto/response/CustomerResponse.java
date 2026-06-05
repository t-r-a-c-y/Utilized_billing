package com.utility.billing.dto.response;

import com.utility.billing.entity.enums.CustomerStatus;

public record CustomerResponse(
        Long id,
        String fullNames,
        String nationalId,
        String email,
        String phoneNumber,
        String address,
        CustomerStatus status,
        int meterCount
) {}
