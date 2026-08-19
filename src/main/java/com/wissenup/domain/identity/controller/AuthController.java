package com.wissenup.domain.identity.controller;

import com.wissenup.domain.identity.dto.LoginResponse;
import com.wissenup.domain.identity.dto.OtpRequest;
import com.wissenup.shared.exception.ValidationException;
import com.wissenup.domain.identity.service.AuthService;
import com.wissenup.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication endpoints (login, OTP verification)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Step 1: Initiate login by sending OTP to email.
     * POST /api/v1/auth/login?email=user@example.com
     */
    @PostMapping("/login")
    @Operation(summary = "Initiate login (send OTP)", description = "Sends OTP code to user's email")
    public ResponseEntity<ApiResponse<Void>> initiateLogin(
            @RequestParam @NotBlank(message = "Email is required") @Email(message = "Valid email is required") String email) {
        authService.initiateLogin(email);
        return ResponseEntity.ok(ApiResponse.success("OTP sent to email. Please verify to complete login."));
    }

    /**
     * Step 2: Verify OTP and complete login.
     * POST /api/v1/auth/otp/verify
     * Body: { email, otp }
     */
    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP and login", description = "Verifies OTP and returns JWT token on success")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(@Valid @RequestBody OtpRequest request) {
        if (!isValidOtp(request.getOtp())) {
            throw new ValidationException("OTP must be exactly 6 digits");
        }
        LoginResponse response = authService.verifyOtpAndLogin(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Resend OTP to email.
     * POST /api/v1/auth/otp/resend?email=user@example.com
     */
    @PostMapping("/otp/resend")
    @Operation(summary = "Resend OTP", description = "Sends a new OTP code to user's email")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @RequestParam @NotBlank(message = "Email is required") @Email(message = "Valid email is required") String email) {
        authService.resendOtp(email);
        return ResponseEntity.ok(ApiResponse.success("OTP resent to email"));
    }

    private boolean isValidOtp(String otp) {
        return otp != null && otp.matches("^\\d{6}$");
    }
}
