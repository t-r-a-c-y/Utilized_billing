package com.utility.billing.controller;

import com.utility.billing.dto.request.PaymentRequest;
import com.utility.billing.dto.response.PaymentResponse;
import com.utility.billing.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "8. Payments", description = "Record and view payments (ROLE_FINANCE)")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Record a payment (FINANCE/ADMIN)",
            description = "Supports partial and full payments. Cannot exceed the outstanding "
                    + "balance. Marks the bill PAID when the balance reaches zero.")
    @PreAuthorize("hasAnyRole('FINANCE','ADMIN')")
    @PostMapping
    public ResponseEntity<PaymentResponse> record(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.record(request));
    }

    @Operation(summary = "View MY payment history (CUSTOMER)",
            description = "Returns the payments of the logged-in customer, resolved from the JWT.")
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    public List<PaymentResponse> getMyPayments(Authentication authentication) {
        return paymentService.getMyPayments(authentication.getName());
    }

    @Operation(summary = "List payments for a bill (staff)")
    @PreAuthorize("hasAnyRole('FINANCE','ADMIN')")
    @GetMapping("/bill/{reference}")
    public List<PaymentResponse> getByBill(@PathVariable String reference) {
        return paymentService.getByBill(reference);
    }

    @Operation(summary = "List a customer's payment history (staff)")
    @PreAuthorize("hasAnyRole('FINANCE','ADMIN')")
    @GetMapping("/customer/{customerId}")
    public List<PaymentResponse> getByCustomer(@PathVariable Long customerId) {
        return paymentService.getByCustomer(customerId);
    }

    @Operation(summary = "List all payments")
    @PreAuthorize("hasAnyRole('FINANCE','ADMIN')")
    @GetMapping
    public List<PaymentResponse> getAll() {
        return paymentService.getAll();
    }
}
