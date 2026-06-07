package com.utility.billing.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Writes security/business audit events to the dedicated "AUDIT" logger, which
 * Logback routes to logs/audit.log. Each entry records WHO performed the action
 * (the authenticated user, or SYSTEM for unauthenticated/internal flows).
 */
@Component
public class AuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Record an audit event, e.g. record("BILL_GENERATED", "ref=BILL-... amount=5192 customer=Tracy"). */
    public void record(String action, String details) {
        AUDIT.info("actor={} | action={} | {}", currentActor(), action, details);
    }

    private String currentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return "SYSTEM/anonymous";
        }
        return auth.getName();
    }
}
