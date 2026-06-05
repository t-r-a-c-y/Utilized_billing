package com.utility.billing.service;

import com.utility.billing.dto.request.MeterRequest;
import com.utility.billing.dto.response.MeterResponse;

import java.util.List;

public interface MeterService {
    MeterResponse create(MeterRequest request);
    MeterResponse update(Long id, MeterRequest request);
    MeterResponse getById(Long id);
    List<MeterResponse> getAll();
    List<MeterResponse> getByCustomer(Long customerId);
}
