package com.wissenup.domain.identity.service;

import com.wissenup.domain.identity.dto.LoginResponse;

/**
 * Service for authentication operations.
 * Handles login flow and OTP verification leading to JWT token issuance.
 */
public interface AuthService {

    /**
     * Step 1: Initiate login by sending OTP to user's email.
     * Validates email exists and user is ACTIVE, then generates OTP.
     *
     * @param email user email address
     * @throws com.wissenup.domain.identity.exception.AuthenticationException if email not found or user inactive
     */
    void initiateLogin(String email);

    /**
     * Step 2: Verify OTP and complete login, returning JWT token.
     * After successful OTP verification, generates JWT with user's organization and role.
     *
     * @param email user email address
     * @param otp 6-digit code from email
     * @return LoginResponse with JWT token and user details
     * @throws com.wissenup.domain.identity.exception.AuthenticationException if OTP invalid/expired or too many attempts
     */
    LoginResponse verifyOtpAndLogin(String email, String otp);

    /**
     * Resend OTP to user's email.
     * Can be called multiple times; previous OTP is invalidated.
     *
     * @param email user email address
     * @throws com.wissenup.domain.identity.exception.AuthenticationException if email not found
     */
    void resendOtp(String email);
}
