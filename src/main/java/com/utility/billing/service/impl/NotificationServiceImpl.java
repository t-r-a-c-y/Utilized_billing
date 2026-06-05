package com.utility.billing.service.impl;

import com.utility.billing.dto.response.NotificationResponse;
import com.utility.billing.entity.Bill;
import com.utility.billing.entity.Notification;
import com.utility.billing.entity.enums.NotificationStatus;
import com.utility.billing.exception.ResourceNotFoundException;
import com.utility.billing.mapper.EntityMapper;
import com.utility.billing.repository.NotificationRepository;
import com.utility.billing.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void createForBill(Bill bill, String message) {
        Notification notification = Notification.builder()
                .customer(bill.getCustomer())
                .bill(bill)
                .message(message)
                .status(NotificationStatus.PENDING)
                .build();
        notificationRepository.save(notification);
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
    @Transactional
    public NotificationResponse markSent(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        n.setStatus(NotificationStatus.SENT);
        return EntityMapper.toNotificationResponse(notificationRepository.save(n));
    }
}
