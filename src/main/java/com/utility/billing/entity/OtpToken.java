package com.utility.billing.entity;

import com.utility.billing.entity.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A short-lived one-time password issued to a user's email for a single purpose
 * (signup verification, two-step login, or password reset).
 */
@Entity
@Table(name = "otp_tokens", indexes = {
        @Index(name = "idx_otp_email_purpose", columnList = "email,purpose")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The account this code was issued to (email = username). */
    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 12)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private OtpPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Set true once the code has been consumed, so it cannot be reused. */
    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }
}
