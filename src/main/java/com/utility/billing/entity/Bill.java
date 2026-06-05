package com.utility.billing.entity;

import com.utility.billing.entity.enums.BillStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A monthly bill generated from a meter reading.
 */
@Entity
@Table(name = "bills",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_bill_reference", columnNames = "bill_reference"),
                @UniqueConstraint(name = "uk_bill_meter_period",
                        columnNames = {"meter_id", "bill_month", "bill_year"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_reference", nullable = false, unique = true, length = 40)
    private String billReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    // Renamed physical columns: "month"/"year" are reserved in H2/MySQL.
    @Column(name = "bill_month", nullable = false)
    private Integer month;

    @Column(name = "bill_year", nullable = false)
    private Integer year;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal consumption;

    @Column(name = "tariff_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal tariffAmount;

    @Column(name = "service_charge", nullable = false, precision = 14, scale = 2)
    private BigDecimal serviceCharge;

    @Column(name = "tax_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "penalty_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "amount_paid", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "outstanding_balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal outstandingBalance;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BillStatus status = BillStatus.PENDING;
}
