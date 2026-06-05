package com.utility.billing.service.impl;

import com.utility.billing.dto.response.UserResponse;
import com.utility.billing.entity.User;
import com.utility.billing.entity.enums.UserStatus;
import com.utility.billing.exception.ResourceNotFoundException;
import com.utility.billing.mapper.EntityMapper;
import com.utility.billing.repository.UserRepository;
import com.utility.billing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(EntityMapper::toUserResponse).toList();
    }

    @Override
    public UserResponse getById(Long id) {
        return EntityMapper.toUserResponse(find(id));
    }

    @Override
    @Transactional
    public UserResponse updateStatus(Long id, UserStatus status) {
        User user = find(id);
        user.setStatus(status);
        return EntityMapper.toUserResponse(userRepository.save(user));
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
