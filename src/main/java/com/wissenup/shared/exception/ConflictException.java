package com.wissenup.shared.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super("CONFLICT", message, HttpStatus.CONFLICT);
    }

    public ConflictException(String resourceType, String identifier) {
        super(
            "CONFLICT",
            String.format("%s with identifier '%s' already exists", resourceType, identifier),
            HttpStatus.CONFLICT
        );
    }

    public static ConflictException duplicate(String resourceType, String field, String value) {
        return new ConflictException(
            String.format("%s with %s '%s' already exists", resourceType, field, value)
        );
    }
}
