package com.utility.billing.service;

import com.utility.billing.dto.response.UserResponse;
import com.utility.billing.entity.enums.UserStatus;

import java.util.List;

public interface UserService {
    List<UserResponse> getAll();
    UserResponse getById(Long id);
    UserResponse updateStatus(Long id, UserStatus status);
}
