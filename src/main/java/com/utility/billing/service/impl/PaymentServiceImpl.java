package com.utility.billing.service.impl;

import com.utility.billing.dto.request.PaymentRequest;
import com.utility.billing.dto.response.PaymentResponse;
import com.utility.billing.entity.Bill;
import com.utility.billing.entity.Payment;
import com.utility.billing.entity.enums.BillStatus;
import com.utility.billing.exception.BusinessRuleException;
import com.utility.billing.exception.ResourceNotFoundException;
import com.utility.billing.mapper.EntityMapper;
import com.utility.billing.repository.BillRepository;
import com.utility.billing.repository.PaymentRepository;
import com.utility.billing.service.NotificationService;
import com.utility.billing.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PaymentResponse record(PaymentRequest request) {
        Bill bill = billRepository.findByBillReference(request.billReference())
                .orElseThrow(() -> new ResourceNotFoundException("Bill", request.billReference()));

        // A bill must be approved before it can be paid (PENDING bills are not payable).
        if (bill.getStatus() == BillStatus.PENDING) {
            throw new BusinessRuleException(
                    "Bill " + bill.getBillReference() + " is not yet approved and cannot be paid");
        }
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BusinessRuleException(
                    "Bill " + bill.getBillReference() + " is already fully paid");
        }

        BigDecimal amount = request.amountPaid();

        // Rule: a bill cannot be paid more than its outstanding balance.
        if (amount.compareTo(bill.getOutstandingBalance()) > 0) {
            throw new BusinessRuleException(
                    "Payment " + amount + " exceeds the outstanding balance of "
                            + bill.getOutstandingBalance());
        }

        // Apply payment and recompute balance.
        bill.setAmountPaid(bill.getAmountPaid().add(amount));
        bill.setOutstandingBalance(bill.getOutstandingBalance().subtract(amount));

        Payment payment = Payment.builder()
                .bill(bill)
                .amountPaid(amount)
                .paymentMethod(request.paymentMethod())
                .paymentDate(request.paymentDate() != null ? request.paymentDate() : LocalDate.now())
                .transactionReference(request.transactionReference())
                .build();
        paymentRepository.save(payment);

        // Rule: PAID when balance is zero; otherwise PARTIALLY_PAID.
        if (bill.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 0) {
            bill.setStatus(BillStatus.PAID);
            // Requirement: on full payment, update bill status and notify customer.
            notificationService.createForBill(bill, buildPaidMessage(bill));
        } else {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
        }
        billRepository.save(bill);

        return EntityMapper.toPaymentResponse(payment);
    }

    @Override
    public List<PaymentResponse> getByBill(String billReference) {
        Bill bill = billRepository.findByBillReference(billReference)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", billReference));
        return paymentRepository.findByBillId(bill.getId()).stream()
                .map(EntityMapper::toPaymentResponse).toList();
    }

    @Override
    public List<PaymentResponse> getByCustomer(Long customerId) {
        return paymentRepository.findByBillCustomerId(customerId).stream()
                .map(EntityMapper::toPaymentResponse).toList();
    }

    @Override
    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll().stream().map(EntityMapper::toPaymentResponse).toList();
    }

    private String buildPaidMessage(Bill bill) {
        return String.format("Dear %s,%nYour %02d/%d utility bill of %s FRW has been fully paid. Thank you.",
                bill.getCustomer().getFullNames(), bill.getMonth(), bill.getYear(),
                bill.getTotalAmount().toPlainString());
    }
}
