package com.utility.billing.service.impl;

import com.utility.billing.dto.response.NotificationResponse;
import com.utility.billing.entity.Bill;
import com.utility.billing.entity.Notification;
import com.utility.billing.entity.enums.NotificationStatus;
import com.utility.billing.exception.ResourceNotFoundException;
import com.utility.billing.mapper.EntityMapper;
import com.utility.billing.repository.NotificationRepository;
import com.utility.billing.service.EmailService;
import com.utility.billing.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final com.utility.billing.security.CurrentCustomerResolver currentCustomer;

    @Override
    @Transactional
    public void createForBill(Bill bill, String message) {
        // Persist the notification AND email it to the customer. EmailService
        // swallows failures (e.g. SMTP not configured), so this never blocks.
        Notification notification = Notification.builder()
                .customer(bill.getCustomer())
                .bill(bill)
                .message(message)
                .status(NotificationStatus.PENDING)
                .build();

        emailService.send(bill.getCustomer().getEmail(), "Utility Billing Notification", message);
        notification.setStatus(NotificationStatus.SENT);

        notificationRepository.save(notification);
        log.info("Notification stored + emailed to {} for bill {}",
                bill.getCustomer().getEmail(), bill.getBillReference());
    }

    @Override
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll().stream()
                .map(EntityMapper::toNotificationResponse).toList();
    }

    @Override
    public List<NotificationResponse> getByCustomer(Long customerId) {
        return notificationRepository.findByCustomerId(customerId).stream()
                .map(EntityMapper::toNotificationResponse).toList();
    }

    @Override
    public List<NotificationResponse> getMyNotifications(String userEmail) {
        Long customerId = currentCustomer.resolve(userEmail).getId();
        return getByCustomer(customerId);
    }

    @Override
    @Transactional
    public NotificationResponse markSent(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        n.setStatus(NotificationStatus.SENT);
        return EntityMapper.toNotificationResponse(notificationRepository.save(n));
    }
}
