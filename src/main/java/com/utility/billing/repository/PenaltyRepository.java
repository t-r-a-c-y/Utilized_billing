package com.utility.billing.repository;

import com.utility.billing.entity.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    @Query("""
            SELECT p FROM Penalty p
            WHERE p.effectiveStart <= :date
              AND (p.effectiveEnd IS NULL OR p.effectiveEnd >= :date)
            ORDER BY p.version DESC
            """)
    Optional<Penalty> findEffective(@Param("date") LocalDate date);

    Optional<Penalty> findTopByOrderByVersionDesc();
}
