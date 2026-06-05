package com.utility.billing.controller;

import com.utility.billing.dto.response.NotificationResponse;
import com.utility.billing.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @Operation(summary = "View MY notifications (CUSTOMER)",
            description = "Returns the notifications of the logged-in customer, resolved from the JWT.")
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    public List<NotificationResponse> getMyNotifications(Authentication authentication) {
        return notificationService.getMyNotifications(authentication.getName());
    }

    @Operation(summary = "List a customer's notifications (staff)")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
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
