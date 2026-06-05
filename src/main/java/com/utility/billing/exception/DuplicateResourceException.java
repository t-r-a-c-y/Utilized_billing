package com.utility.billing.exception;

/** Thrown when a uniqueness constraint would be violated. Maps to HTTP 409. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
