package com.utility.billing.controller;

import com.utility.billing.dto.request.PenaltyRequest;
import com.utility.billing.dto.request.TariffRequest;
import com.utility.billing.dto.request.TaxRequest;
import com.utility.billing.dto.response.PenaltyResponse;
import com.utility.billing.dto.response.TariffResponse;
import com.utility.billing.dto.response.TaxResponse;
import com.utility.billing.service.TariffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "6. Tariffs, Taxes & Penalties", description = "Versioned configuration (ROLE_ADMIN)")
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class TariffController {

    private final TariffService tariffService;

    // ----- Tariffs -----
    @Operation(summary = "Create a tariff version (ADMIN)",
            description = "Closes the previous active version for the same meter type. "
                    + "effectiveStart should be in the future so it only affects future cycles.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/tariffs")
    public ResponseEntity<TariffResponse> createTariff(@Valid @RequestBody TariffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tariffService.createTariff(request));
    }

    @Operation(summary = "List all tariff versions")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @GetMapping("/tariffs")
    public List<TariffResponse> getTariffs() {
        return tariffService.getAllTariffs();
    }

    @Operation(summary = "Get a tariff by id")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @GetMapping("/tariffs/{id}")
    public TariffResponse getTariff(@PathVariable Long id) {
        return tariffService.getTariff(id);
    }

    // ----- Taxes -----
    @Operation(summary = "Create a tax version, e.g. VAT (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/taxes")
    public ResponseEntity<TaxResponse> createTax(@Valid @RequestBody TaxRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tariffService.createTax(request));
    }

    @Operation(summary = "List all tax versions")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @GetMapping("/taxes")
    public List<TaxResponse> getTaxes() {
        return tariffService.getAllTaxes();
    }

    // ----- Penalties -----
    @Operation(summary = "Create a late-payment penalty version (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/penalties")
    public ResponseEntity<PenaltyResponse> createPenalty(@Valid @RequestBody PenaltyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tariffService.createPenalty(request));
    }

    @Operation(summary = "List all penalty versions")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @GetMapping("/penalties")
    public List<PenaltyResponse> getPenalties() {
        return tariffService.getAllPenalties();
    }
}
