package com.utility.billing.service.impl;

import com.utility.billing.dto.request.*;
import com.utility.billing.dto.response.AuthResponse;
import com.utility.billing.dto.response.MessageResponse;
import com.utility.billing.dto.response.OtpChallengeResponse;
import com.utility.billing.entity.Customer;
import com.utility.billing.entity.User;
import com.utility.billing.entity.enums.CustomerStatus;
import com.utility.billing.entity.enums.OtpPurpose;
import com.utility.billing.entity.enums.Role;
import com.utility.billing.entity.enums.UserStatus;
import com.utility.billing.exception.BusinessRuleException;
import com.utility.billing.exception.DuplicateResourceException;
import com.utility.billing.exception.ResourceNotFoundException;
import com.utility.billing.repository.CustomerRepository;
import com.utility.billing.repository.UserRepository;
import com.utility.billing.security.JwtService;
import com.utility.billing.service.AuthService;
import com.utility.billing.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication, OTP-based account verification, two-step login and password
 * reset. Customers self-register here: a ROLE_CUSTOMER signup also creates and
 * links a Customer profile. Staff accounts (OPERATOR/FINANCE) are created by an
 * admin via the same endpoint but without a customer profile.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final OtpService otpService;

    // ----------------------------------------------------------- Signup + verify

    @Override
    @Transactional
    public OtpChallengeResponse signup(SignupRequest request) {
        final String email = normalize(request.email());

        // Rule: duplicate users (by email) are not allowed.
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User already exists with email: " + email);
        }

        // Customers self-register and get a linked Customer profile.
        Customer customer = null;
        if (request.role() == Role.ROLE_CUSTOMER) {
            customer = createCustomerProfile(request, email);
        }

        // Account starts INACTIVE until the email OTP is verified.
        User user = User.builder()
                .fullNames(request.fullNames())
                .email(email)
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(UserStatus.INACTIVE)
                .customer(customer)
                .build();
        userRepository.save(user);
        log.info("Registered new {} account: {}", request.role(), email);

        int minutes = otpService.issue(email, OtpPurpose.SIGNUP_VERIFICATION);
        return new OtpChallengeResponse(email, OtpPurpose.SIGNUP_VERIFICATION.name(), true, minutes,
                "Account created. An OTP has been emailed; verify it at /api/v1/auth/verify-account to activate.");
    }

    private Customer createCustomerProfile(SignupRequest request, String email) {
        if (request.nationalId() == null || request.nationalId().isBlank()
                || request.address() == null || request.address().isBlank()) {
            throw new BusinessRuleException(
                    "National ID and address are required when registering as a customer.");
        }
        if (customerRepository.existsByNationalId(request.nationalId())) {
            throw new DuplicateResourceException(
                    "Customer already exists with National ID: " + request.nationalId());
        }
        if (customerRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Customer already exists with email: " + email);
        }
        return customerRepository.save(Customer.builder()
                .fullNames(request.fullNames())
                .nationalId(request.nationalId())
                .email(email)
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .status(CustomerStatus.ACTIVE)
                .build());
    }

    @Override
    @Transactional
    public MessageResponse verifyAccount(OtpVerificationRequest request) {
        final String email = normalize(request.email());
        User user = findUser(email);
        otpService.verify(email, request.otp(), OtpPurpose.SIGNUP_VERIFICATION);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("Account verified and activated: {}", email);
        return new MessageResponse("Account verified successfully. You can now log in.");
    }

    // ----------------------------------------------------------- Two-step login

    @Override
    public OtpChallengeResponse login(LoginRequest request) {
        final String email = normalize(request.email());
        // Validates password AND that the account is enabled (ACTIVE). Inactive or
        // unverified accounts are rejected here, then a login OTP is emailed.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        log.info("Password accepted for {}, issuing login OTP", email);

        int minutes = otpService.issue(email, OtpPurpose.LOGIN);
        return new OtpChallengeResponse(email, OtpPurpose.LOGIN.name(), true, minutes,
                "Credentials accepted. An OTP has been emailed; submit it to /api/v1/auth/verify-otp to receive your token.");
    }

    @Override
    public AuthResponse verifyLoginOtp(OtpVerificationRequest request) {
        final String email = normalize(request.email());
        otpService.verify(email, request.otp(), OtpPurpose.LOGIN);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails);
        String role = userDetails.getAuthorities().stream().findFirst()
                .map(Object::toString).orElse("");
        log.info("Login OTP verified, JWT issued for {}", email);
        return AuthResponse.of(token, userDetails.getUsername(), role, jwtService.getExpirationMs());
    }

    // --------------------------------------------------------- Password reset

    @Override
    public OtpChallengeResponse forgotPassword(ForgotPasswordRequest request) {
        final String email = normalize(request.email());
        findUser(email); // ensure the account exists
        int minutes = otpService.issue(email, OtpPurpose.PASSWORD_RESET);
        return new OtpChallengeResponse(email, OtpPurpose.PASSWORD_RESET.name(), true, minutes,
                "A password-reset OTP has been emailed; submit it with your new password to /api/v1/auth/reset-password.");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        final String email = normalize(request.email());
        User user = findUser(email);
        otpService.verify(email, request.otp(), OtpPurpose.PASSWORD_RESET);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password reset for {}", email);
        return new MessageResponse("Password reset successfully. You can now log in with your new password.");
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /** Emails are stored and compared in lowercase (rule: email must be lowercase). */
    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
