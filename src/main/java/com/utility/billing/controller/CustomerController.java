package com.utility.billing.controller;

import com.utility.billing.dto.request.CustomerRequest;
import com.utility.billing.dto.response.CustomerResponse;
import com.utility.billing.entity.enums.CustomerStatus;
import com.utility.billing.service.CustomerService;
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

@Tag(name = "3. Customers", description = "Customer registration and management")
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Register a new customer (ADMIN)",
            description = "National ID and email must be unique.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @Operation(summary = "Update a customer (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @Operation(summary = "View MY profile (CUSTOMER)",
            description = "Returns the logged-in customer's own profile, resolved from the JWT.")
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/me")
    public CustomerResponse getMyProfile(Authentication authentication) {
        return customerService.getMyProfile(authentication.getName());
    }

    @Operation(summary = "List all customers")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @GetMapping
    public List<CustomerResponse> getAll() {
        return customerService.getAll();
    }

    @Operation(summary = "Get a customer by id")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @GetMapping("/{id}")
    public CustomerResponse getById(@PathVariable Long id) {
        return customerService.getById(id);
    }

    @Operation(summary = "Activate / deactivate a customer (ADMIN)",
            description = "Customers are never deleted — to preserve audit history (bills, payments, "
                    + "notifications) an admin sets the status to ACTIVE or INACTIVE. "
                    + "Inactive customers cannot receive new bills.")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public CustomerResponse updateStatus(@PathVariable Long id, @RequestParam CustomerStatus status) {
        return customerService.updateStatus(id, status);
    }
}
