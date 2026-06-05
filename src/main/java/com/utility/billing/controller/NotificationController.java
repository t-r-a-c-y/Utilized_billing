package com.utility.billing.controller;

import com.utility.billing.dto.response.NotificationResponse;
import com.utility.billing.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "9. Notifications", description = "Customer notification messages")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "List all notifications (ADMIN/FINANCE)")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @GetMapping
    public List<NotificationResponse> getAll() {
        return notificationService.getAll();
    }

    @Operation(summary = "List a customer's notifications")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @GetMapping("/customer/{customerId}")
    public List<NotificationResponse> getByCustomer(@PathVariable Long customerId) {
        return notificationService.getByCustomer(customerId);
    }

    @Operation(summary = "Mark a notification as SENT (ADMIN/FINANCE)",
            description = "Simulates dispatch by an external SMS/email gateway.")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @PatchMapping("/{id}/sent")
    public NotificationResponse markSent(@PathVariable Long id) {
        return notificationService.markSent(id);
    }
}
