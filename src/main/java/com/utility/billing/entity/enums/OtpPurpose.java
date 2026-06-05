package com.utility.billing.entity.enums;

/**
 * What an OTP code is for. A code is only valid for the purpose it was issued.
 */
public enum OtpPurpose {
    SIGNUP_VERIFICATION,  // activate a newly registered account
    LOGIN,                // second factor after password check
    PASSWORD_RESET        // authorise setting a new password
}
