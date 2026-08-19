package com.wissenup.domain.identity.service;

/**
 * Service for generating and verifying OTP codes.
 * Handles both OTP generation and validation logic.
 */
public interface OtpService {

    /**
     * Generate a 6-digit OTP and store it for the given email.
     * OTP is valid for the duration specified in configuration.
     *
     * @param email user email address
     * @throws com.wissenup.domain.identity.exception.AuthenticationException if OTP generation fails
     */
    void generateAndSendOtp(String email);

    /**
     * Verify the OTP code for the given email.
     * Checks if code is valid and not expired, and increments attempt counter.
     *
     * @param email user email address
     * @param otp 6-digit code entered by user
     * @return true if OTP is valid and not expired
     * @throws com.wissenup.domain.identity.exception.AuthenticationException if OTP is invalid, expired, or max attempts exceeded
     */
    boolean verifyOtp(String email, String otp);

    /**
     * Clear the OTP for the given email (called after successful verification).
     *
     * @param email user email address
     */
    void clearOtp(String email);
}
