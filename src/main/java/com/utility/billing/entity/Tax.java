package com.utility.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A versioned tax (e.g. VAT) expressed as a percentage of the taxable amount.
 */
@Entity
@Table(name = "taxes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tax extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;   // e.g. "VAT"

    /** Percentage, e.g. 18.00 means 18%. */
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal percentage;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "effective_start", nullable = false)
    private LocalDate effectiveStart;

    @Column(name = "effective_end")
    private LocalDate effectiveEnd;
}
