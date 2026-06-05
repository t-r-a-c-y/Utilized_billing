package com.utility.billing.controller;

import com.utility.billing.dto.request.BillGenerateRequest;
import com.utility.billing.dto.response.BillResponse;
import com.utility.billing.service.BillService;
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

@Tag(name = "7. Bills", description = "Bill generation, approval and lookup")
@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @Operation(summary = "Generate a bill from a reading (ADMIN/OPERATOR)",
            description = "Computes tariff, service charge and tax, sets the due date and "
                    + "creates a notification. Inactive customers cannot be billed.")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @PostMapping("/generate")
    public ResponseEntity<BillResponse> generate(@Valid @RequestBody BillGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.generate(request));
    }

    @Operation(summary = "Approve a bill (ADMIN/FINANCE)",
            description = "Moves a PENDING bill to APPROVED so payments can be recorded.")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @PatchMapping("/{id}/approve")
    public BillResponse approve(@PathVariable Long id) {
        return billService.approve(id);
    }

    @Operation(summary = "Run overdue check and apply penalties (ADMIN/FINANCE)")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @PostMapping("/apply-overdue")
    public List<BillResponse> applyOverdue() {
        return billService.applyOverduePenalties();
    }

    @Operation(summary = "List all bills")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @GetMapping
    public List<BillResponse> getAll() {
        return billService.getAll();
    }

    @Operation(summary = "View MY bills (CUSTOMER)",
            description = "Returns the bills of the logged-in customer, resolved from the JWT — "
                    + "a customer can only ever see their own bills.")
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    public List<BillResponse> getMyBills(Authentication authentication) {
        return billService.getMyBills(authentication.getName());
    }

    @Operation(summary = "Get a bill by id (staff)")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @GetMapping("/{id}")
    public BillResponse getById(@PathVariable Long id) {
        return billService.getById(id);
    }

    @Operation(summary = "Get a bill by reference (staff)")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @GetMapping("/reference/{reference}")
    public BillResponse getByReference(@PathVariable String reference) {
        return billService.getByReference(reference);
    }

    @Operation(summary = "List a customer's bills (staff)",
            description = "Staff lookup by customer id. Customers use GET /bills/my instead.")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @GetMapping("/customer/{customerId}")
    public List<BillResponse> getByCustomer(@PathVariable Long customerId) {
        return billService.getByCustomer(customerId);
    }
}
