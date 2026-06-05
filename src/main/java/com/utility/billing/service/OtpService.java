package com.utility.billing.service;

import com.utility.billing.entity.enums.OtpPurpose;

public interface OtpService {

    /** Generate, persist and email a fresh OTP for the given email/purpose.
     *  @return the number of minutes the code remains valid. */
    int issue(String email, OtpPurpose purpose);

    /** Validate a submitted code for the email/purpose; marks it used on success.
     *  Throws BusinessRuleException if missing, wrong, expired or already used. */
    void verify(String email, String code, OtpPurpose purpose);
}
