package com.utility.billing.entity;

import com.utility.billing.entity.enums.MeterType;
import com.utility.billing.entity.enums.TariffType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A versioned tariff for a meter type. Each new configuration is a new version
 * with an effective start date; older versions are closed with an end date.
 * Billing always selects the tariff whose [start, end] window covers the
 * billing cycle date, guaranteeing new tariffs only apply to future cycles.
 */
@Entity
@Table(name = "tariffs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tariff extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "meter_type", nullable = false, length = 15)
    private MeterType meterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "tariff_type", nullable = false, length = 10)
    private TariffType tariffType;

    /** Monotonic version number per meter type. */
    @Column(nullable = false)
    private Integer version;

    /** Used when tariffType == FLAT: price per unit. */
    @Column(name = "rate_per_unit", precision = 14, scale = 4)
    private BigDecimal ratePerUnit;

    /** Fixed monthly service charge applied to every bill. */
    @Column(name = "service_charge", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal serviceCharge = BigDecimal.ZERO;

    @Column(name = "effective_start", nullable = false)
    private LocalDate effectiveStart;

    /** Null = currently active (open-ended). */
    @Column(name = "effective_end")
    private LocalDate effectiveEnd;

    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("upToUnit asc")
    @Builder.Default
    private List<TariffTier> tiers = new ArrayList<>();
}
