package com.utility.billing.dto.response;

import com.utility.billing.entity.enums.Role;
import com.utility.billing.entity.enums.UserStatus;

public record UserResponse(
        Long id,
        String fullNames,
        String email,
        String phoneNumber,
        Role role,
        UserStatus status,
        Long customerId
) {}
