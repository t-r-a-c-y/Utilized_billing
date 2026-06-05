package com.utility.billing.entity;

import com.utility.billing.entity.enums.MeterStatus;
import com.utility.billing.entity.enums.MeterType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * A physical water or electricity meter belonging to a customer.
 */
@Entity
@Table(name = "meters",
        uniqueConstraints = @UniqueConstraint(name = "uk_meter_number", columnNames = "meter_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meter_number", nullable = false, unique = true, length = 40)
    private String meterNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "meter_type", nullable = false, length = 15)
    private MeterType meterType;

    @Column(name = "installation_date", nullable = false)
    private LocalDate installationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private MeterStatus status = MeterStatus.ACTIVE;

    // Nullable: an admin can create an UNASSIGNED meter that a customer later
    // claims by its meter number. A meter belongs to at most one customer.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
