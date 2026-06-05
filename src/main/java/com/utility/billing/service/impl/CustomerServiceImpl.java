package com.utility.billing.service.impl;

import com.utility.billing.dto.request.CustomerRequest;
import com.utility.billing.dto.response.CustomerResponse;
import com.utility.billing.entity.Customer;
import com.utility.billing.entity.enums.CustomerStatus;
import com.utility.billing.exception.DuplicateResourceException;
import com.utility.billing.exception.ResourceNotFoundException;
import com.utility.billing.mapper.EntityMapper;
import com.utility.billing.repository.CustomerRepository;
import com.utility.billing.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final com.utility.billing.security.CurrentCustomerResolver currentCustomer;

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        // Rule: prevent duplicate customer registration (by National ID and email).
        if (customerRepository.existsByNationalId(request.nationalId())) {
            throw new DuplicateResourceException(
                    "Customer already exists with National ID: " + request.nationalId());
        }
        if (customerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(
                    "Customer already exists with email: " + request.email());
        }

        Customer customer = Customer.builder()
                .fullNames(request.fullNames())
                .nationalId(request.nationalId())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .status(request.status() != null ? request.status() : CustomerStatus.ACTIVE)
                .build();

        return EntityMapper.toCustomerResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = find(id);

        // Guard against changing National ID / email into another customer's value.
        if (!customer.getNationalId().equals(request.nationalId())
                && customerRepository.existsByNationalId(request.nationalId())) {
            throw new DuplicateResourceException(
                    "Another customer already uses National ID: " + request.nationalId());
        }
        if (!customer.getEmail().equals(request.email())
                && customerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(
                    "Another customer already uses email: " + request.email());
        }

        customer.setFullNames(request.fullNames());
        customer.setNationalId(request.nationalId());
        customer.setEmail(request.email());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setAddress(request.address());
        if (request.status() != null) {
            customer.setStatus(request.status());
        }
        return EntityMapper.toCustomerResponse(customerRepository.save(customer));
    }

    @Override
    public CustomerResponse getById(Long id) {
        return EntityMapper.toCustomerResponse(find(id));
    }

    @Override
    public List<CustomerResponse> getAll() {
        return customerRepository.findAll().stream().map(EntityMapper::toCustomerResponse).toList();
    }

    @Override
    public CustomerResponse getMyProfile(String userEmail) {
        return EntityMapper.toCustomerResponse(currentCustomer.resolve(userEmail));
    }

    @Override
    @Transactional
    public CustomerResponse updateStatus(Long id, CustomerStatus status) {
        // Soft state change only — the customer row and its history are kept.
        Customer customer = find(id);
        customer.setStatus(status);
        return EntityMapper.toCustomerResponse(customerRepository.save(customer));
    }

    private Customer find(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}
