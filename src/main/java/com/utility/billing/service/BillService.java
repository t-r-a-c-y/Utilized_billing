package com.utility.billing.service;

import com.utility.billing.dto.request.BillGenerateRequest;
import com.utility.billing.dto.response.BillResponse;

import java.util.List;

public interface BillService {
    BillResponse generate(BillGenerateRequest request);
    BillResponse approve(Long billId);
    BillResponse getById(Long id);
    BillResponse getByReference(String reference);
    List<BillResponse> getAll();
    List<BillResponse> getByCustomer(Long customerId);

    /** Re-evaluate due dates: mark approved/partially-paid bills past due as OVERDUE and apply penalty. */
    List<BillResponse> applyOverduePenalties();
}
