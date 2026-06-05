package com.utility.billing.exception;

/** Thrown when a domain/business rule is violated. Maps to HTTP 422. */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
