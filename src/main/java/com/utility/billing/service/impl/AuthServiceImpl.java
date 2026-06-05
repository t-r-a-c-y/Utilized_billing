package com.utility.billing.service.impl;

import com.utility.billing.dto.request.*;
import com.utility.billing.dto.response.AuthResponse;
import com.utility.billing.dto.response.MessageResponse;
import com.utility.billing.dto.response.OtpChallengeResponse;
import com.utility.billing.entity.User;
import com.utility.billing.entity.enums.OtpPurpose;
import com.utility.billing.entity.enums.UserStatus;
import com.utility.billing.exception.DuplicateResourceException;
import com.utility.billing.exception.ResourceNotFoundException;
import com.utility.billing.repository.UserRepository;
import com.utility.billing.security.JwtService;
import com.utility.billing.service.AuthService;
import com.utility.billing.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final OtpService otpService;

    // ----------------------------------------------------------- Signup + verify

    @Override
    @Transactional
    public OtpChallengeResponse signup(SignupRequest request) {
        // Rule: duplicate users (by email) are not allowed.
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User already exists with email: " + request.email());
        }

        // Account starts INACTIVE until the email OTP is verified.
        User user = User.builder()
                .fullNames(request.fullNames())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(UserStatus.INACTIVE)
                .build();
        userRepository.save(user);

        int minutes = otpService.issue(request.email(), OtpPurpose.SIGNUP_VERIFICATION);
        return new OtpChallengeResponse(request.email(), OtpPurpose.SIGNUP_VERIFICATION.name(), true, minutes,
                "Account created. An OTP has been emailed; verify it at /api/v1/auth/verify-account to activate.");
    }

    @Override
    @Transactional
    public MessageResponse verifyAccount(OtpVerificationRequest request) {
        User user = findUser(request.email());
        otpService.verify(request.email(), request.otp(), OtpPurpose.SIGNUP_VERIFICATION);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return new MessageResponse("Account verified successfully. You can now log in.");
    }

    // ----------------------------------------------------------- Two-step login

    @Override
    public OtpChallengeResponse login(LoginRequest request) {
        // Validates password AND that the account is enabled (ACTIVE). Inactive or
        // unverified accounts are rejected here, then a login OTP is emailed.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        int minutes = otpService.issue(request.email(), OtpPurpose.LOGIN);
        return new OtpChallengeResponse(request.email(), OtpPurpose.LOGIN.name(), true, minutes,
                "Credentials accepted. An OTP has been emailed; submit it to /api/v1/auth/verify-otp to receive your token.");
    }

    @Override
    public AuthResponse verifyLoginOtp(OtpVerificationRequest request) {
        otpService.verify(request.email(), request.otp(), OtpPurpose.LOGIN);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.generateToken(userDetails);
        String role = userDetails.getAuthorities().stream().findFirst()
                .map(Object::toString).orElse("");
        return AuthResponse.of(token, userDetails.getUsername(), role, jwtService.getExpirationMs());
    }

    // --------------------------------------------------------- Password reset

    @Override
    public OtpChallengeResponse forgotPassword(ForgotPasswordRequest request) {
        findUser(request.email()); // ensure the account exists
        int minutes = otpService.issue(request.email(), OtpPurpose.PASSWORD_RESET);
        return new OtpChallengeResponse(request.email(), OtpPurpose.PASSWORD_RESET.name(), true, minutes,
                "A password-reset OTP has been emailed; submit it with your new password to /api/v1/auth/reset-password.");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = findUser(request.email());
        otpService.verify(request.email(), request.otp(), OtpPurpose.PASSWORD_RESET);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return new MessageResponse("Password reset successfully. You can now log in with your new password.");
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
