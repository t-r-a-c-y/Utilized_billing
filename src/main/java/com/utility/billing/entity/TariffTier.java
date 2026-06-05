package com.utility.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * A single block/tier of a TIERED tariff, e.g. "0-10 units @ 320 FRW".
 * Tiers are evaluated in ascending order of upToUnit.
 */
@Entity
@Table(name = "tariff_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    /** Upper bound (inclusive) of units for this tier. Null = no upper bound (top tier). */
    @Column(name = "up_to_unit", precision = 14, scale = 2)
    private BigDecimal upToUnit;

    @Column(name = "rate_per_unit", nullable = false, precision = 14, scale = 4)
    private BigDecimal ratePerUnit;
}
