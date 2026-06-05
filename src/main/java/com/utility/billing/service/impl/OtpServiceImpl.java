package com.utility.billing.service.impl;

import com.utility.billing.entity.OtpToken;
import com.utility.billing.entity.enums.OtpPurpose;
import com.utility.billing.exception.BusinessRuleException;
import com.utility.billing.repository.OtpTokenRepository;
import com.utility.billing.service.EmailService;
import com.utility.billing.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpRepository;
    private final EmailService emailService;
    private final int expiryMinutes;
    private final int length;
    private final boolean logToConsole;

    private final SecureRandom random = new SecureRandom();

    public OtpServiceImpl(OtpTokenRepository otpRepository,
                          EmailService emailService,
                          @Value("${app.otp.expiry-minutes:10}") int expiryMinutes,
                          @Value("${app.otp.length:6}") int length,
                          @Value("${app.otp.log-to-console:true}") boolean logToConsole) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.expiryMinutes = expiryMinutes;
        this.length = length;
        this.logToConsole = logToConsole;
    }

    @Override
    @Transactional
    public int issue(String email, OtpPurpose purpose) {
        // Only one active code per email/purpose at a time.
        otpRepository.invalidateOutstanding(email, purpose);

        String code = generateCode();
        OtpToken token = OtpToken.builder()
                .email(email)
                .code(code)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .build();
        otpRepository.save(token);

        if (logToConsole) {
            log.info("OTP for {} [{}] = {} (valid {} min)", email, purpose, code, expiryMinutes);
        }
        emailService.send(email, subjectFor(purpose), bodyFor(purpose, code));
        return expiryMinutes;
    }

    @Override
    @Transactional
    public void verify(String email, String code, OtpPurpose purpose) {
        OtpToken token = otpRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new BusinessRuleException(
                        "No active OTP found. Please request a new code."));

        if (token.isExpired(LocalDateTime.now())) {
            throw new BusinessRuleException("OTP has expired. Please request a new code.");
        }
        if (!token.getCode().equals(code)) {
            throw new BusinessRuleException("Invalid OTP code.");
        }

        token.setUsed(true);
        otpRepository.save(token);
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, length);
        int value = random.nextInt(bound);
        return String.format("%0" + length + "d", value);
    }

    private String subjectFor(OtpPurpose purpose) {
        return switch (purpose) {
            case SIGNUP_VERIFICATION -> "Verify your Utility Billing account";
            case LOGIN -> "Your Utility Billing login code";
            case PASSWORD_RESET -> "Reset your Utility Billing password";
        };
    }

    private String bodyFor(OtpPurpose purpose, String code) {
        String action = switch (purpose) {
            case SIGNUP_VERIFICATION -> "verify your account";
            case LOGIN -> "complete your login";
            case PASSWORD_RESET -> "reset your password";
        };
        return "Dear user,\n\nUse the code below to " + action + ":\n\n    "
                + code + "\n\nThis code expires in " + expiryMinutes + " minutes. "
                + "If you did not request this, please ignore this email.\n\n- Utility Billing System";
    }
}
