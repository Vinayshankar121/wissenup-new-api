package com.wissenup.domain.identity.exception;

import com.wissenup.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Thrown when authentication fails (invalid email/password or OTP).
 */
public class AuthenticationException extends ApiException {

    public AuthenticationException(String message) {
        super("AUTHENTICATION_FAILED", message, HttpStatus.UNAUTHORIZED);
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid email or password");
    }

    public static AuthenticationException otpExpired() {
        return new AuthenticationException("OTP has expired. Please request a new code");
    }

    public static AuthenticationException otpInvalid() {
        return new AuthenticationException("Invalid OTP. Please try again");
    }

    public static AuthenticationException tooManyAttempts() {
        return new AuthenticationException("Too many incorrect attempts. Please request a new code");
    }
}
