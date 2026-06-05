package com.utility.billing.service.impl;

import com.utility.billing.dto.request.MeterRequest;
import com.utility.billing.dto.response.MeterResponse;
import com.utility.billing.entity.Customer;
import com.utility.billing.entity.Meter;
import com.utility.billing.entity.enums.MeterStatus;
import com.utility.billing.exception.DuplicateResourceException;
import com.utility.billing.exception.ResourceNotFoundException;
import com.utility.billing.mapper.EntityMapper;
import com.utility.billing.repository.CustomerRepository;
import com.utility.billing.repository.MeterRepository;
import com.utility.billing.service.MeterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterServiceImpl implements MeterService {

    private final MeterRepository meterRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public MeterResponse create(MeterRequest request) {
        // Rule: duplicate meters by meter number are not allowed.
        if (meterRepository.existsByMeterNumber(request.meterNumber())) {
            throw new DuplicateResourceException(
                    "Meter already exists with number: " + request.meterNumber());
        }
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));

        Meter meter = Meter.builder()
                .meterNumber(request.meterNumber())
                .meterType(request.meterType())
                .installationDate(request.installationDate())
                .status(request.status() != null ? request.status() : MeterStatus.ACTIVE)
                .customer(customer)
                .build();

        return EntityMapper.toMeterResponse(meterRepository.save(meter));
    }

    @Override
    @Transactional
    public MeterResponse update(Long id, MeterRequest request) {
        Meter meter = find(id);

        if (!meter.getMeterNumber().equals(request.meterNumber())
                && meterRepository.existsByMeterNumber(request.meterNumber())) {
            throw new DuplicateResourceException(
                    "Another meter already uses number: " + request.meterNumber());
        }
        if (!meter.getCustomer().getId().equals(request.customerId())) {
            Customer customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
            meter.setCustomer(customer);
        }

        meter.setMeterNumber(request.meterNumber());
        meter.setMeterType(request.meterType());
        meter.setInstallationDate(request.installationDate());
        if (request.status() != null) {
            meter.setStatus(request.status());
        }
        return EntityMapper.toMeterResponse(meterRepository.save(meter));
    }

    @Override
    public MeterResponse getById(Long id) {
        return EntityMapper.toMeterResponse(find(id));
    }

    @Override
    public List<MeterResponse> getAll() {
        return meterRepository.findAll().stream().map(EntityMapper::toMeterResponse).toList();
    }

    @Override
    public List<MeterResponse> getByCustomer(Long customerId) {
        return meterRepository.findByCustomerId(customerId).stream()
                .map(EntityMapper::toMeterResponse).toList();
    }

    private Meter find(Long id) {
        return meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter", id));
    }
}
