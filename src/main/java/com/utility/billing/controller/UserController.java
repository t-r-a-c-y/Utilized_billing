package com.utility.billing.controller;

import com.utility.billing.dto.response.UserResponse;
import com.utility.billing.entity.enums.UserStatus;
import com.utility.billing.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "2. Users", description = "User administration (ROLE_ADMIN)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @Operation(summary = "List all users")
    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @Operation(summary = "Get a user by id")
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @Operation(summary = "Activate or deactivate a user",
            description = "Inactive users cannot authenticate.")
    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable Long id, @RequestParam UserStatus status) {
        return userService.updateStatus(id, status);
    }
}
