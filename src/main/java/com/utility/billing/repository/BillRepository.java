package com.utility.billing.repository;

import com.utility.billing.entity.Bill;
import com.utility.billing.entity.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    boolean existsByMeterIdAndMonthAndYear(Long meterId, Integer month, Integer year);

    Optional<Bill> findByBillReference(String billReference);

    List<Bill> findByCustomerId(Long customerId);

    List<Bill> findByStatus(BillStatus status);
}
