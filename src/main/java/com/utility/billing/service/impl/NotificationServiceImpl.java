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

import java.math.BigDecimal;
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
        // Persist the required-format notification AND email the customer a detailed
        // bill. EmailService swallows failures, so this never blocks the transaction.
        Notification notification = Notification.builder()
                .customer(bill.getCustomer())
                .bill(bill)
                .message(message)
                .status(NotificationStatus.PENDING)
                .build();

        String subject = "Utility Bill " + bill.getBillReference();
        emailService.send(bill.getCustomer().getEmail(), subject, buildBillEmail(bill, message));
        notification.setStatus(NotificationStatus.SENT);

        notificationRepository.save(notification);
        log.info("Notification stored + emailed to {} for bill {}",
                bill.getCustomer().getEmail(), bill.getBillReference());
    }

    @Override
    public void emailPaymentReceipt(Bill bill, BigDecimal amountPaid) {
        String name = bill.getCustomer().getFullNames();
        BigDecimal outstanding = bill.getOutstandingBalance();
        String body;
        if (outstanding.compareTo(BigDecimal.ZERO) == 0) {
            body = String.format(
                    "Dear %s,%n%nWe have received your payment of %s FRW for bill %s.%n"
                    + "Your %02d/%d bill of %s FRW is now FULLY PAID. Thank you.%n%n- Utility Billing System",
                    name, amountPaid.toPlainString(), bill.getBillReference(),
                    bill.getMonth(), bill.getYear(), bill.getTotalAmount().toPlainString());
        } else {
            body = String.format(
                    "Dear %s,%n%nWe have received your payment of %s FRW for bill %s.%n"
                    + "Remaining balance to pay: %s FRW (due by %s).%n%n- Utility Billing System",
                    name, amountPaid.toPlainString(), bill.getBillReference(),
                    outstanding.toPlainString(), bill.getDueDate());
        }
        emailService.send(bill.getCustomer().getEmail(),
                "Payment received - " + bill.getBillReference(), body);
        log.info("Payment receipt emailed to {} for bill {} (paid {}, outstanding {})",
                bill.getCustomer().getEmail(), bill.getBillReference(), amountPaid, outstanding);
    }

    /** Detailed, customer-friendly bill email body: what they owe and by when. */
    private String buildBillEmail(Bill bill, String headline) {
        return headline + System.lineSeparator() + System.lineSeparator()
                + "Bill reference   : " + bill.getBillReference() + System.lineSeparator()
                + String.format("Billing period   : %02d/%d", bill.getMonth(), bill.getYear()) + System.lineSeparator()
                + "Consumption      : " + bill.getConsumption() + " units" + System.lineSeparator()
                + "Total amount     : " + bill.getTotalAmount().toPlainString() + " FRW" + System.lineSeparator()
                + "Amount paid      : " + bill.getAmountPaid().toPlainString() + " FRW" + System.lineSeparator()
                + "Amount to pay    : " + bill.getOutstandingBalance().toPlainString() + " FRW" + System.lineSeparator()
                + "Due date         : " + bill.getDueDate() + System.lineSeparator()
                + System.lineSeparator()
                + "Please settle the outstanding amount before the due date to avoid penalties."
                + System.lineSeparator() + "- Utility Billing System";
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
