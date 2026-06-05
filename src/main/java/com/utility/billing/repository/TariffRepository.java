package com.utility.billing.repository;

import com.utility.billing.entity.Tariff;
import com.utility.billing.entity.enums.MeterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TariffRepository extends JpaRepository<Tariff, Long> {

    /**
     * Resolve the tariff version effective for a given meter type on a billing
     * cycle date: effectiveStart <= date AND (effectiveEnd is null OR date <= effectiveEnd).
     */
    @Query("""
            SELECT t FROM Tariff t
            WHERE t.meterType = :meterType
              AND t.effectiveStart <= :date
              AND (t.effectiveEnd IS NULL OR t.effectiveEnd >= :date)
            ORDER BY t.version DESC
            """)
    Optional<Tariff> findEffective(@Param("meterType") MeterType meterType,
                                   @Param("date") LocalDate date);

    Optional<Tariff> findTopByMeterTypeOrderByVersionDesc(MeterType meterType);
}
