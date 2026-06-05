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

    /** Bills belonging to the logged-in customer (resolved from their JWT email). */
    List<BillResponse> getMyBills(String userEmail);

    /** Re-evaluate due dates: mark approved/partially-paid bills past due as OVERDUE and apply penalty. */
    List<BillResponse> applyOverduePenalties();
}
