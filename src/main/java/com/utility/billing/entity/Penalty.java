package com.utility.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A versioned late-payment penalty, expressed as a percentage of the
 * outstanding balance charged when a bill becomes overdue.
 */
@Entity
@Table(name = "penalties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Penalty extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;   // e.g. "Late payment penalty"

    /** Percentage of outstanding balance, e.g. 5.00 means 5%. */
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal percentage;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "effective_start", nullable = false)
    private LocalDate effectiveStart;

    @Column(name = "effective_end")
    private LocalDate effectiveEnd;
}
