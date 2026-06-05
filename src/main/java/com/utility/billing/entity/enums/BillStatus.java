package com.utility.billing.entity.enums;

public enum BillStatus {
    PENDING,         // generated, awaiting approval
    APPROVED,        // approved by ADMIN/FINANCE, payable
    PARTIALLY_PAID,  // some amount paid, balance remaining
    PAID,            // fully settled
    OVERDUE          // past due date with outstanding balance
}
