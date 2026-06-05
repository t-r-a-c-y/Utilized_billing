package com.utility.billing.repository;

import com.utility.billing.entity.OtpToken;
import com.utility.billing.entity.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    /** Latest unused code issued to this email for the given purpose. */
    Optional<OtpToken> findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String email, OtpPurpose purpose);

    /** Invalidate any outstanding codes before issuing a fresh one. */
    @Modifying
    @Query("UPDATE OtpToken o SET o.used = true WHERE o.email = :email AND o.purpose = :purpose AND o.used = false")
    void invalidateOutstanding(@Param("email") String email, @Param("purpose") OtpPurpose purpose);
}
