package com.utility.billing.mapper;

import com.utility.billing.dto.response.*;
import com.utility.billing.entity.*;

/**
 * Stateless mapping methods between entities and response DTOs.
 * Kept as static helpers so they can be reused from any service without DI.
 */
public final class EntityMapper {

    private EntityMapper() {}

    public static UserResponse toUserResponse(User u) {
        return new UserResponse(
                u.getId(), u.getFullNames(), u.getEmail(), u.getPhoneNumber(),
                u.getRole(), u.getStatus(),
                u.getCustomer() != null ? u.getCustomer().getId() : null);
    }

    public static CustomerResponse toCustomerResponse(Customer c) {
        return new CustomerResponse(
                c.getId(), c.getFullNames(), c.getNationalId(), c.getEmail(),
                c.getPhoneNumber(), c.getAddress(), c.getStatus(),
                c.getMeters() != null ? c.getMeters().size() : 0);
    }

    public static MeterResponse toMeterResponse(Meter m) {
        Customer c = m.getCustomer();   // may be null for an unassigned meter
        return new MeterResponse(
                m.getId(), m.getMeterNumber(), m.getMeterType(), m.getInstallationDate(),
                m.getStatus(),
                c != null ? c.getId() : null,
                c != null ? c.getFullNames() : null);
    }

    public static MeterReadingResponse toReadingResponse(MeterReading r) {
        return new MeterReadingResponse(
                r.getId(), r.getMeter().getId(), r.getMeter().getMeterNumber(),
                r.getPreviousReading(), r.getCurrentReading(), r.getConsumption(),
                r.getReadingDate(), r.getMonth(), r.getYear(), r.isBilled());
    }

    public static TariffResponse toTariffResponse(Tariff t) {
        var tiers = t.getTiers().stream()
                .map(tier -> new TariffResponse.TariffTierResponse(
                        tier.getId(), tier.getUpToUnit(), tier.getRatePerUnit()))
                .toList();
        return new TariffResponse(
                t.getId(), t.getName(), t.getMeterType(), t.getTariffType(), t.getVersion(),
                t.getRatePerUnit(), t.getServiceCharge(), t.getEffectiveStart(),
                t.getEffectiveEnd(), tiers);
    }

    public static TaxResponse toTaxResponse(Tax t) {
        return new TaxResponse(t.getId(), t.getName(), t.getPercentage(), t.getVersion(),
                t.getEffectiveStart(), t.getEffectiveEnd());
    }

    public static PenaltyResponse toPenaltyResponse(Penalty p) {
        return new PenaltyResponse(p.getId(), p.getName(), p.getPercentage(), p.getVersion(),
                p.getEffectiveStart(), p.getEffectiveEnd());
    }

    public static BillResponse toBillResponse(Bill b) {
        return new BillResponse(
                b.getId(), b.getBillReference(),
                b.getCustomer().getId(), b.getCustomer().getFullNames(),
                b.getMeter().getId(), b.getMeter().getMeterNumber(),
                b.getMonth(), b.getYear(), b.getConsumption(),
                b.getTariffAmount(), b.getServiceCharge(), b.getTaxAmount(),
                b.getPenaltyAmount(), b.getTotalAmount(), b.getAmountPaid(),
                b.getOutstandingBalance(), b.getDueDate(), b.getStatus());
    }

    public static PaymentResponse toPaymentResponse(Payment p) {
        Bill b = p.getBill();
        return new PaymentResponse(
                p.getId(), b.getBillReference(), p.getAmountPaid(), p.getPaymentMethod(),
                p.getPaymentDate(), p.getTransactionReference(),
                b.getOutstandingBalance(), b.getStatus().name());
    }

    public static NotificationResponse toNotificationResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getCustomer().getId(), n.getCustomer().getFullNames(),
                n.getBill() != null ? n.getBill().getId() : null,
                n.getMessage(), n.getStatus(), n.getCreatedAt());
    }
}
