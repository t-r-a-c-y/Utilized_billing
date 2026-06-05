package com.utility.billing.entity.enums;

/**
 * System roles. Stored with the Spring Security "ROLE_" prefix so they can be
 * used directly in @PreAuthorize / hasRole() checks.
 */
public enum Role {
    ROLE_ADMIN,      // configure tariffs, approve bills, manage users
    ROLE_OPERATOR,   // capture meter readings
    ROLE_FINANCE,    // approve bills and payments
    ROLE_CUSTOMER    // view own bills and payment history
}
