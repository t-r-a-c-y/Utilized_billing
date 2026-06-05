package com.utility.billing.service.impl;

import com.utility.billing.dto.request.BillGenerateRequest;
import com.utility.billing.dto.response.BillResponse;
import com.utility.billing.entity.*;
import com.utility.billing.entity.enums.BillStatus;
import com.utility.billing.entity.enums.CustomerStatus;
import com.utility.billing.entity.enums.TariffType;
import com.utility.billing.exception.BusinessRuleException;
import com.utility.billing.exception.DuplicateResourceException;
import com.utility.billing.exception.ResourceNotFoundException;
import com.utility.billing.mapper.EntityMapper;
import com.utility.billing.repository.*;
import com.utility.billing.service.BillService;
import com.utility.billing.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private static final int DEFAULT_DUE_DAYS = 15;

    private final BillRepository billRepository;
    private final MeterReadingRepository readingRepository;
    private final TariffRepository tariffRepository;
    private final TaxRepository taxRepository;
    private final PenaltyRepository penaltyRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public BillResponse generate(BillGenerateRequest request) {
        // Locate the reading for the requested meter/period.
        MeterReading reading = readingRepository
                .findByMeterIdAndMonthAndYear(request.meterId(), request.month(), request.year())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No meter reading found for meter " + request.meterId()
                                + " in " + request.month() + "/" + request.year()));

        Meter meter = reading.getMeter();
        Customer customer = meter.getCustomer();

        // Rule: inactive customers cannot receive bills.
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Customer " + customer.getFullNames() + " is INACTIVE and cannot be billed");
        }

        // Rule: one bill per meter/month/year.
        if (billRepository.existsByMeterIdAndMonthAndYear(meter.getId(), request.month(), request.year())) {
            throw new DuplicateResourceException(
                    "A bill already exists for meter " + meter.getMeterNumber()
                            + " in " + request.month() + "/" + request.year());
        }

        // Billing cycle date drives versioned tariff/tax selection (last day of the period).
        LocalDate cycleDate = LocalDate.of(request.year(), request.month(), 1)
                .withDayOfMonth(java.time.YearMonth.of(request.year(), request.month()).lengthOfMonth());

        Tariff tariff = tariffRepository.findEffective(meter.getMeterType(), cycleDate)
                .orElseThrow(() -> new BusinessRuleException(
                        "No effective tariff configured for " + meter.getMeterType()
                                + " on " + cycleDate));

        BigDecimal consumption = reading.getConsumption();
        BigDecimal tariffAmount = computeTariffAmount(tariff, consumption);
        BigDecimal serviceCharge = nz(tariff.getServiceCharge());

        // Tax applies to (tariff + service charge).
        BigDecimal taxableBase = tariffAmount.add(serviceCharge);
        BigDecimal taxAmount = taxRepository.findEffective(cycleDate)
                .map(tax -> percentage(taxableBase, tax.getPercentage()))
                .orElse(BigDecimal.ZERO);

        BigDecimal total = tariffAmount.add(serviceCharge).add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        int dueDays = request.dueInDays() != null ? request.dueInDays() : DEFAULT_DUE_DAYS;

        Bill bill = Bill.builder()
                .billReference(buildReference(request.year(), request.month()))
                .customer(customer)
                .meter(meter)
                .month(request.month())
                .year(request.year())
                .consumption(consumption)
                .tariffAmount(tariffAmount)
                .serviceCharge(serviceCharge)
                .taxAmount(taxAmount)
                .penaltyAmount(BigDecimal.ZERO)
                .totalAmount(total)
                .amountPaid(BigDecimal.ZERO)
                .outstandingBalance(total)
                .dueDate(cycleDate.plusDays(dueDays))
                .status(BillStatus.PENDING)
                .build();

        bill = billRepository.save(bill);

        reading.setBilled(true);
        readingRepository.save(reading);

        // Requirement: on bill generation, insert a notification message.
        notificationService.createForBill(bill, buildBillMessage(bill));

        return EntityMapper.toBillResponse(bill);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public BillResponse approve(Long billId) {
        Bill bill = find(billId);
        if (bill.getStatus() != BillStatus.PENDING) {
            throw new BusinessRuleException(
                    "Only PENDING bills can be approved; current status is " + bill.getStatus());
        }
        bill.setStatus(BillStatus.APPROVED);
        return EntityMapper.toBillResponse(billRepository.save(bill));
    }

    @Override
    public BillResponse getById(Long id) {
        return EntityMapper.toBillResponse(find(id));
    }

    @Override
    public BillResponse getByReference(String reference) {
        return EntityMapper.toBillResponse(billRepository.findByBillReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", reference)));
    }

    @Override
    public List<BillResponse> getAll() {
        return billRepository.findAll().stream().map(EntityMapper::toBillResponse).toList();
    }

    @Override
    public List<BillResponse> getByCustomer(Long customerId) {
        return billRepository.findByCustomerId(customerId).stream()
                .map(EntityMapper::toBillResponse).toList();
    }

    @Override
    @Transactional
    public List<BillResponse> applyOverduePenalties() {
        LocalDate today = LocalDate.now();
        Penalty penalty = penaltyRepository.findEffective(today).orElse(null);

        List<Bill> candidates = billRepository.findAll().stream()
                .filter(b -> b.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0)
                .filter(b -> b.getStatus() == BillStatus.APPROVED
                        || b.getStatus() == BillStatus.PARTIALLY_PAID
                        || b.getStatus() == BillStatus.OVERDUE)
                .filter(b -> b.getDueDate().isBefore(today))
                .toList();

        for (Bill bill : candidates) {
            if (bill.getStatus() != BillStatus.OVERDUE && penalty != null) {
                BigDecimal penaltyAmount = percentage(bill.getOutstandingBalance(), penalty.getPercentage());
                bill.setPenaltyAmount(nz(bill.getPenaltyAmount()).add(penaltyAmount));
                bill.setTotalAmount(bill.getTotalAmount().add(penaltyAmount));
                bill.setOutstandingBalance(bill.getOutstandingBalance().add(penaltyAmount));
            }
            bill.setStatus(BillStatus.OVERDUE);
            billRepository.save(bill);
        }
        return candidates.stream().map(EntityMapper::toBillResponse).toList();
    }

    // ------------------------------------------------------------- calculation

    /** FLAT: rate * units. TIERED: walk the blocks in ascending order. */
    private BigDecimal computeTariffAmount(Tariff tariff, BigDecimal consumption) {
        if (tariff.getTariffType() == TariffType.FLAT) {
            return consumption.multiply(tariff.getRatePerUnit()).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal remaining = consumption;
        BigDecimal lowerBound = BigDecimal.ZERO;
        BigDecimal amount = BigDecimal.ZERO;

        for (TariffTier tier : tariff.getTiers()) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal blockUnits;
            if (tier.getUpToUnit() == null) {
                blockUnits = remaining; // open-ended top tier
            } else {
                BigDecimal tierCapacity = tier.getUpToUnit().subtract(lowerBound);
                blockUnits = remaining.min(tierCapacity);
                lowerBound = tier.getUpToUnit();
            }
            amount = amount.add(blockUnits.multiply(tier.getRatePerUnit()));
            remaining = remaining.subtract(blockUnits);
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentage(BigDecimal base, BigDecimal pct) {
        return base.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String buildReference(int year, int month) {
        // BILL-2026-05-000001 style; sequence approximated by current count + 1.
        long seq = billRepository.count() + 1;
        return String.format("BILL-%04d-%02d-%06d", year, month, seq);
    }

    private String buildBillMessage(Bill bill) {
        return String.format("Dear %s,%nYour %02d/%d utility bill of %s FRW has been successfully processed.",
                bill.getCustomer().getFullNames(), bill.getMonth(), bill.getYear(),
                bill.getTotalAmount().toPlainString());
    }

    private Bill find(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", id));
    }
}
