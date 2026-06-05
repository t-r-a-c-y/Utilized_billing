package com.utility.billing.service;

import com.utility.billing.dto.request.MeterReadingRequest;
import com.utility.billing.dto.response.MeterReadingResponse;

import java.util.List;

public interface MeterReadingService {
    MeterReadingResponse capture(MeterReadingRequest request);
    MeterReadingResponse getById(Long id);
    List<MeterReadingResponse> getByMeter(Long meterId);
    List<MeterReadingResponse> getAll();
}
