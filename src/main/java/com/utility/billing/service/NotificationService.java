package com.utility.billing.service;

import com.utility.billing.dto.response.NotificationResponse;
import com.utility.billing.entity.Bill;

import java.math.BigDecimal;
import java.util.List;

public interface NotificationService {

    /** Persist a notification for a bill AND email the customer a detailed bill
     *  (amount due, outstanding, due date). Used on generation and on full payment. */
    void createForBill(Bill bill, String message);

    /** Email the customer a payment receipt (used on every payment, partial or full).
     *  Does not store a DB notification — kept aligned with the Task 6 routines which
     *  only fire on bill generation and full payment. */
    void emailPaymentReceipt(Bill bill, BigDecimal amountPaid);

    List<NotificationResponse> getAll();

    List<NotificationResponse> getByCustomer(Long customerId);

    /** Notifications for the logged-in customer (resolved from their JWT email). */
    List<NotificationResponse> getMyNotifications(String userEmail);

    /** Simulate dispatch: flip PENDING notifications to SENT. */
    NotificationResponse markSent(Long id);
}
