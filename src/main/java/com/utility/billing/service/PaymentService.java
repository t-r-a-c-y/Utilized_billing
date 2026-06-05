package com.utility.billing.service;

import com.utility.billing.dto.request.PaymentRequest;
import com.utility.billing.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse record(PaymentRequest request);
    List<PaymentResponse> getByBill(String billReference);
    List<PaymentResponse> getByCustomer(Long customerId);
    List<PaymentResponse> getAll();

    /** Payment history of the logged-in customer (resolved from their JWT email). */
    List<PaymentResponse> getMyPayments(String userEmail);
}
