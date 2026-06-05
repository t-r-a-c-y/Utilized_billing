package com.utility.billing.service.impl;

import com.utility.billing.dto.request.MeterRequest;
import com.utility.billing.dto.response.MeterResponse;
import com.utility.billing.entity.Customer;
import com.utility.billing.entity.Meter;
import com.utility.billing.entity.User;
import com.utility.billing.entity.enums.MeterStatus;
import com.utility.billing.exception.BusinessRuleException;
import com.utility.billing.exception.DuplicateResourceException;
import com.utility.billing.exception.ResourceNotFoundException;
import com.utility.billing.mapper.EntityMapper;
import com.utility.billing.repository.CustomerRepository;
import com.utility.billing.repository.MeterRepository;
import com.utility.billing.repository.UserRepository;
import com.utility.billing.service.MeterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeterServiceImpl implements MeterService {

    private final MeterRepository meterRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MeterResponse create(MeterRequest request) {
        // Rule: duplicate meters by meter number are not allowed.
        if (meterRepository.existsByMeterNumber(request.meterNumber())) {
            throw new DuplicateResourceException(
                    "Meter already exists with number: " + request.meterNumber());
        }

        // customerId is optional: admins may create an UNASSIGNED meter that a
        // customer later claims by its number.
        Customer customer = null;
        if (request.customerId() != null) {
            customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        }

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
    public MeterResponse claim(String meterNumber, String userEmail) {
        User user = userRepository.findByEmail(userEmail == null ? null : userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Customer customer = user.getCustomer();
        if (customer == null) {
            throw new BusinessRuleException(
                    "Your account has no customer profile, so it cannot own a meter.");
        }

        Meter meter = meterRepository.findByMeterNumber(meterNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No meter found with number: " + meterNumber));

        // Rule: a meter can belong to at most one customer.
        if (meter.getCustomer() != null) {
            if (meter.getCustomer().getId().equals(customer.getId())) {
                throw new BusinessRuleException("You already own meter " + meterNumber + ".");
            }
            throw new BusinessRuleException(
                    "Meter " + meterNumber + " is already assigned to another customer.");
        }

        meter.setCustomer(customer);   // a customer may own many meters
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

        // Re-assign owner only when a (different) customerId is supplied.
        Long currentCustomerId = meter.getCustomer() != null ? meter.getCustomer().getId() : null;
        if (request.customerId() != null && !request.customerId().equals(currentCustomerId)) {
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
