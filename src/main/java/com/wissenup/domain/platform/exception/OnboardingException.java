package com.wissenup.domain.platform.exception;

import com.wissenup.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

public class OnboardingException extends ApiException {
    public OnboardingException(String message) {
        super("ONBOARDING_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    public OnboardingException(String message, Throwable cause) {
        super("ONBOARDING_ERROR", message, HttpStatus.BAD_REQUEST, cause);
    }
}
