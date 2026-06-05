package com.utility.billing.service;

import com.utility.billing.dto.request.CustomerRequest;
import com.utility.billing.dto.response.CustomerResponse;
import com.utility.billing.entity.enums.CustomerStatus;

import java.util.List;

public interface CustomerService {
    CustomerResponse create(CustomerRequest request);
    CustomerResponse update(Long id, CustomerRequest request);
    CustomerResponse getById(Long id);
    List<CustomerResponse> getAll();

    /**
     * Activate or deactivate a customer. Customers are never hard-deleted so the
     * audit history (bills, payments, notifications) is preserved. Inactive
     * customers cannot receive new bills.
     */
    CustomerResponse updateStatus(Long id, CustomerStatus status);
}
