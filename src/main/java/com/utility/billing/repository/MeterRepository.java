package com.utility.billing.repository;

import com.utility.billing.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeterRepository extends JpaRepository<Meter, Long> {
    boolean existsByMeterNumber(String meterNumber);
    Optional<Meter> findByMeterNumber(String meterNumber);
    List<Meter> findByCustomerId(Long customerId);
}
