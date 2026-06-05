package com.utility.billing.service.impl;

import com.utility.billing.dto.request.MeterReadingRequest;
import com.utility.billing.dto.response.MeterReadingResponse;
import com.utility.billing.entity.Meter;
import com.utility.billing.entity.MeterReading;
import com.utility.billing.entity.enums.MeterStatus;
import com.utility.billing.exception.BusinessRuleException;
import com.utility.billing.exception.DuplicateResourceException;
import com.utility.billing.exception.ResourceNotFoundException;
import com.utility.billing.mapper.EntityMapper;
import com.utility.billing.repository.MeterReadingRepository;
import com.utility.billing.repository.MeterRepository;
import com.utility.billing.service.MeterReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterReadingServiceImpl implements MeterReadingService {

    private final MeterReadingRepository readingRepository;
    private final MeterRepository meterRepository;

    @Override
    @Transactional
    public MeterReadingResponse capture(MeterReadingRequest request) {
        Meter meter = meterRepository.findById(request.meterId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter", request.meterId()));

        // Rule: inactive meters cannot receive readings.
        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Meter " + meter.getMeterNumber() + " is INACTIVE and cannot receive readings");
        }

        // Rule: only one reading per meter per month/year.
        if (readingRepository.existsByMeterIdAndMonthAndYear(
                meter.getId(), request.month(), request.year())) {
            throw new DuplicateResourceException(
                    "A reading already exists for meter " + meter.getMeterNumber()
                            + " in " + request.month() + "/" + request.year());
        }

        // Determine previous reading: explicit value, else last recorded, else zero.
        BigDecimal previous = request.previousReading();
        if (previous == null) {
            previous = readingRepository.findTopByMeterIdOrderByYearDescMonthDesc(meter.getId())
                    .map(MeterReading::getCurrentReading)
                    .orElse(BigDecimal.ZERO);
        }

        // Rule: current reading must be greater than previous reading.
        if (request.currentReading().compareTo(previous) <= 0) {
            throw new BusinessRuleException(
                    "Current reading (" + request.currentReading()
                            + ") must be greater than previous reading (" + previous + ")");
        }

        BigDecimal consumption = request.currentReading().subtract(previous);

        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(previous)
                .currentReading(request.currentReading())
                .consumption(consumption)
                .readingDate(request.readingDate())
                .month(request.month())
                .year(request.year())
                .billed(false)
                .build();

        return EntityMapper.toReadingResponse(readingRepository.save(reading));
    }

    @Override
    public MeterReadingResponse getById(Long id) {
        return EntityMapper.toReadingResponse(find(id));
    }

    @Override
    public List<MeterReadingResponse> getByMeter(Long meterId) {
        return readingRepository.findByMeterIdOrderByYearDescMonthDesc(meterId).stream()
                .map(EntityMapper::toReadingResponse).toList();
    }

    @Override
    public List<MeterReadingResponse> getAll() {
        return readingRepository.findAll().stream().map(EntityMapper::toReadingResponse).toList();
    }

    private MeterReading find(Long id) {
        return readingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MeterReading", id));
    }
}
