package com.utility.billing.repository;

import com.utility.billing.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

    boolean existsByMeterIdAndMonthAndYear(Long meterId, Integer month, Integer year);

    Optional<MeterReading> findByMeterIdAndMonthAndYear(Long meterId, Integer month, Integer year);

    List<MeterReading> findByMeterIdOrderByYearDescMonthDesc(Long meterId);

    /** Latest reading for a meter, used to seed the next previous-reading. */
    Optional<MeterReading> findTopByMeterIdOrderByYearDescMonthDesc(Long meterId);
}
