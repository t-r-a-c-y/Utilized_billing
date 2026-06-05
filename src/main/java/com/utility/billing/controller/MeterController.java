package com.utility.billing.controller;

import com.utility.billing.dto.request.MeterRequest;
import com.utility.billing.dto.response.MeterResponse;
import com.utility.billing.service.MeterService;
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

@Tag(name = "4. Meters", description = "Meter registration and management")
@RestController
@RequestMapping("/api/v1/meters")
@RequiredArgsConstructor
public class MeterController {

    private final MeterService meterService;

    @Operation(summary = "Register a new meter (ADMIN)",
            description = "Meter number must be unique. customerId is optional — omit it to create an "
                    + "UNASSIGNED meter that a customer can later claim by its number.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MeterResponse> create(@Valid @RequestBody MeterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(meterService.create(request));
    }

    @Operation(summary = "Claim a meter by its number (CUSTOMER)",
            description = "The logged-in customer claims an unassigned meter by entering its meter "
                    + "number. A customer may own several meters; a meter can belong to only one customer.")
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/claim/{meterNumber}")
    public MeterResponse claim(@PathVariable String meterNumber, Authentication authentication) {
        return meterService.claim(meterNumber, authentication.getName());
    }

    @Operation(summary = "Update a meter (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public MeterResponse update(@PathVariable Long id, @Valid @RequestBody MeterRequest request) {
        return meterService.update(id, request);
    }

    @Operation(summary = "List all meters")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @GetMapping
    public List<MeterResponse> getAll() {
        return meterService.getAll();
    }

    @Operation(summary = "Get a meter by id")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @GetMapping("/{id}")
    public MeterResponse getById(@PathVariable Long id) {
        return meterService.getById(id);
    }

    @Operation(summary = "List meters belonging to a customer")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @GetMapping("/customer/{customerId}")
    public List<MeterResponse> getByCustomer(@PathVariable Long customerId) {
        return meterService.getByCustomer(customerId);
    }
}
