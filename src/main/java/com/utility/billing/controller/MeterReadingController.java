package com.utility.billing.controller;

import com.utility.billing.dto.request.MeterReadingRequest;
import com.utility.billing.dto.response.MeterReadingResponse;
import com.utility.billing.service.MeterReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "5. Meter Readings", description = "Capture and view monthly readings (ROLE_OPERATOR)")
@RestController
@RequestMapping("/api/v1/readings")
@RequiredArgsConstructor
public class MeterReadingController {

    private final MeterReadingService readingService;

    @Operation(summary = "Capture a meter reading (OPERATOR/ADMIN)",
            description = "Meter must be ACTIVE; current > previous; one reading per meter per month/year.")
    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
    @PostMapping
    public ResponseEntity<MeterReadingResponse> capture(@Valid @RequestBody MeterReadingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(readingService.capture(request));
    }

    @Operation(summary = "Get a reading by id")
    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN','FINANCE')")
    @GetMapping("/{id}")
    public MeterReadingResponse getById(@PathVariable Long id) {
        return readingService.getById(id);
    }

    @Operation(summary = "List readings for a meter")
    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN','FINANCE')")
    @GetMapping("/meter/{meterId}")
    public List<MeterReadingResponse> getByMeter(@PathVariable Long meterId) {
        return readingService.getByMeter(meterId);
    }

    @Operation(summary = "List all readings")
    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN','FINANCE')")
    @GetMapping
    public List<MeterReadingResponse> getAll() {
        return readingService.getAll();
    }
}
