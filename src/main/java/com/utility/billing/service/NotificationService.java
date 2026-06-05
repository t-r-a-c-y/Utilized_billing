package com.utility.billing.service;

import com.utility.billing.dto.response.NotificationResponse;
import com.utility.billing.entity.Bill;

import java.util.List;

public interface NotificationService {

    /** Persist a notification for a bill (used on generation and on full payment). */
    void createForBill(Bill bill, String message);

    List<NotificationResponse> getAll();

    List<NotificationResponse> getByCustomer(Long customerId);

    /** Notifications for the logged-in customer (resolved from their JWT email). */
    List<NotificationResponse> getMyNotifications(String userEmail);

    /** Simulate dispatch: flip PENDING notifications to SENT. */
    NotificationResponse markSent(Long id);
}
