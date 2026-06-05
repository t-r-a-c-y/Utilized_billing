package com.utility.billing.controller;

import com.utility.billing.dto.request.*;
import com.utility.billing.dto.response.AuthResponse;
import com.utility.billing.dto.response.MessageResponse;
import com.utility.billing.dto.response.OtpChallengeResponse;
import com.utility.billing.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "1. Authentication", description = "Public signup, OTP verification, login & password reset (no token required)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new account",
            description = "Creates an INACTIVE account and emails a verification OTP. "
                    + "Email must be unique. Verify the OTP at /verify-account to activate.")
    @ApiResponse(responseCode = "201", description = "Account created, verification OTP sent")
    @ApiResponse(responseCode = "409", description = "Email already registered")
    @PostMapping("/signup")
    public ResponseEntity<OtpChallengeResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @Operation(summary = "Verify account with signup OTP",
            description = "Activates a newly registered account using the emailed OTP.")
    @ApiResponse(responseCode = "200", description = "Account activated")
    @ApiResponse(responseCode = "422", description = "OTP missing, wrong or expired")
    @PostMapping("/verify-account")
    public MessageResponse verifyAccount(@Valid @RequestBody OtpVerificationRequest request) {
        return authService.verifyAccount(request);
    }

    @Operation(summary = "Login step 1 — credentials",
            description = "Validates email/password (inactive accounts rejected) and emails a login OTP. "
                    + "No token is returned yet; submit the OTP to /verify-otp.")
    @ApiResponse(responseCode = "200", description = "Credentials accepted, login OTP sent")
    @ApiResponse(responseCode = "401", description = "Invalid credentials or inactive account")
    @PostMapping("/login")
    public OtpChallengeResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Login step 2 — verify OTP",
            description = "Validates the emailed login OTP and returns the JWT.")
    @ApiResponse(responseCode = "200", description = "Authenticated; JWT issued")
    @ApiResponse(responseCode = "422", description = "OTP missing, wrong or expired")
    @PostMapping("/verify-otp")
    public AuthResponse verifyLoginOtp(@Valid @RequestBody OtpVerificationRequest request) {
        return authService.verifyLoginOtp(request);
    }

    @Operation(summary = "Forgot password — request reset OTP",
            description = "Emails a password-reset OTP to the account.")
    @PostMapping("/forgot-password")
    public OtpChallengeResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @Operation(summary = "Reset password with OTP",
            description = "Sets a new password using the emailed reset OTP.")
    @ApiResponse(responseCode = "200", description = "Password updated")
    @ApiResponse(responseCode = "422", description = "OTP missing, wrong or expired")
    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }
}
