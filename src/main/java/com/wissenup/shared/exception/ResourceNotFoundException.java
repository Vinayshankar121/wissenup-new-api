package com.wissenup.shared.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resourceType, String id) {
        super(
            "RESOURCE_NOT_FOUND",
            String.format("%s with id '%s' not found", resourceType, id),
            HttpStatus.NOT_FOUND
        );
    }

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }

    public static ResourceNotFoundException notFound(String resourceType, Long id) {
        return new ResourceNotFoundException(
            String.format("%s with id '%d' not found", resourceType, id)
        );
    }
}
