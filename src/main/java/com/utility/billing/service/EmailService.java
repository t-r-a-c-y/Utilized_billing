package com.utility.billing.service;

public interface EmailService {
    /** Send a plain-text email. Failures are logged, not thrown, so OTP flows
     *  still complete in environments without configured SMTP. */
    void send(String to, String subject, String body);
}
