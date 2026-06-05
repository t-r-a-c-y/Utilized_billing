package com.utility.billing.service;

import com.utility.billing.dto.request.CustomerRequest;
import com.utility.billing.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {
    CustomerResponse create(CustomerRequest request);
    CustomerResponse update(Long id, CustomerRequest request);
    CustomerResponse getById(Long id);
    List<CustomerResponse> getAll();
    void delete(Long id);
}
