package com.utility.billing.repository;

import com.utility.billing.entity.Tax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TaxRepository extends JpaRepository<Tax, Long> {

    @Query("""
            SELECT t FROM Tax t
            WHERE t.effectiveStart <= :date
              AND (t.effectiveEnd IS NULL OR t.effectiveEnd >= :date)
            ORDER BY t.version DESC
            """)
    Optional<Tax> findEffective(@Param("date") LocalDate date);

    Optional<Tax> findTopByOrderByVersionDesc();
}
