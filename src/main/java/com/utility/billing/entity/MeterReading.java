package com.utility.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A monthly meter reading captured by an operator. Unique per meter/month/year.
 */
@Entity
@Table(name = "meter_readings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reading_meter_period",
                columnNames = {"meter_id", "reading_month", "reading_year"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReading extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @Column(name = "previous_reading", nullable = false, precision = 14, scale = 2)
    private BigDecimal previousReading;

    @Column(name = "current_reading", nullable = false, precision = 14, scale = 2)
    private BigDecimal currentReading;

    /** currentReading - previousReading, computed by the service. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal consumption;

    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    // "month"/"year" are reserved words in several SQL dialects (H2, MySQL),
    // so the physical columns are renamed while the Java fields stay readable.
    @Column(name = "reading_month", nullable = false)
    private Integer month;   // 1..12

    @Column(name = "reading_year", nullable = false)
    private Integer year;

    /** Set true once a bill has been generated from this reading. */
    @Column(nullable = false)
    @Builder.Default
    private boolean billed = false;
}
