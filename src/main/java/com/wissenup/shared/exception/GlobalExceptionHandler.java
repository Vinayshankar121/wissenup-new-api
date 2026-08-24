package com.wissenup.shared.exception;

import com.wissenup.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Global exception handler for converting exceptions to standardized API responses.
 * Centralizes error handling across all controllers and domains.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Missing API routes/resources are client errors, not internal failures.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoResourceFoundException(
        NoResourceFoundException ex,
        WebRequest request) {

        ApiResponse<?> response = ApiResponse.error(
            "RESOURCE_NOT_FOUND",
            "The requested resource was not found",
            request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle custom ApiException.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<?>> handleApiException(
        ApiException ex,
        WebRequest request) {

        log.warn("API Exception: {} - {}", ex.getCode(), ex.getMessage());

        ApiResponse<?> response = ApiResponse.error(
            ex.getCode(),
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    /**
     * Handle Spring Security access denied.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(
        AccessDeniedException ex,
        WebRequest request) {

        log.warn("Access Denied: {}", ex.getMessage());

        ApiResponse<?> response = ApiResponse.error(
            "ACCESS_DENIED",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handle validation errors from @Valid annotation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        WebRequest request) {

        String errors = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
            .collect(Collectors.joining(", "));

        log.warn("Validation error: {}", errors);

        ApiResponse<?> response = ApiResponse.error(
            "VALIDATION_ERROR",
            "Validation failed: " + errors,
            request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle IllegalArgumentException (bad request).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(
        IllegalArgumentException ex,
        WebRequest request) {

        log.warn("Illegal Argument: {}", ex.getMessage());

        ApiResponse<?> response = ApiResponse.error(
            "INVALID_REQUEST",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle IllegalStateException (conflict).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalStateException(
        IllegalStateException ex,
        WebRequest request) {

        log.warn("Illegal State: {}", ex.getMessage());

        ApiResponse<?> response = ApiResponse.error(
            "INVALID_STATE",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handle all other exceptions (catch-all).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(
        Exception ex,
        WebRequest request) {

        log.error("Unexpected exception", ex);

        ApiResponse<?> response = ApiResponse.error(
            "INTERNAL_ERROR",
            "An unexpected error occurred. Please contact support if the problem persists.",
            request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
