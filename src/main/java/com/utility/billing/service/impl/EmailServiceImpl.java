package com.utility.billing.service.impl;

import com.utility.billing.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends OTP emails over SMTP. If SMTP credentials are not configured (or the
 * send fails), the error is logged rather than thrown — the OTP itself is still
 * persisted and (optionally) printed to the console, so the flow never breaks.
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final boolean mailConfigured;

    public EmailServiceImpl(JavaMailSender mailSender,
                            @Value("${app.otp.from-address:no-reply@utility.rw}") String fromAddress,
                            @Value("${spring.mail.username:}") String mailUsername) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.mailConfigured = mailUsername != null && !mailUsername.isBlank();
    }

    @Override
    public void send(String to, String subject, String body) {
        if (!mailConfigured) {
            log.warn("SMTP not configured (spring.mail.username empty) - skipping real email to {}. "
                    + "Subject: {}", to, subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} ({})", to, subject);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}
