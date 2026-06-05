package com.utility.billing.service;

import com.utility.billing.dto.request.*;
import com.utility.billing.dto.response.AuthResponse;
import com.utility.billing.dto.response.MessageResponse;
import com.utility.billing.dto.response.OtpChallengeResponse;

public interface AuthService {

    /** Register an account (created INACTIVE) and email a verification OTP. */
    OtpChallengeResponse signup(SignupRequest request);

    /** Verify the signup OTP and activate the account. */
    MessageResponse verifyAccount(OtpVerificationRequest request);

    /** Step 1 of login: validate credentials and email a login OTP. */
    OtpChallengeResponse login(LoginRequest request);

    /** Step 2 of login: validate the login OTP and issue a JWT. */
    AuthResponse verifyLoginOtp(OtpVerificationRequest request);

    /** Email a password-reset OTP. */
    OtpChallengeResponse forgotPassword(ForgotPasswordRequest request);

    /** Validate the reset OTP and set a new password. */
    MessageResponse resetPassword(ResetPasswordRequest request);
}
