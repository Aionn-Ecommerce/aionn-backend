package com.ecommerce.sharedkernel.presentation.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ecommerce.sharedkernel.common.exception.ValidationException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        String errorCode,
        String message,
        String domain,
        Map<String, String> fieldErrors,
        Instant timestamp) {
    public static ErrorResponse of(String errorCode, String message, String domain) {
        return new ErrorResponse(false, errorCode, message, domain, null, Instant.now());
    }

    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(false, errorCode, message, null, null, Instant.now());
    }

    public static ErrorResponse withFieldErrors(String message, String domain,
            List<ValidationException.FieldError> errors) {
        Map<String, String> fieldMap = errors.stream()
                .collect(java.util.stream.Collectors.toMap(
                        ValidationException.FieldError::field,
                        ValidationException.FieldError::message,
                        (a, b) -> a + "; " + b));
        return new ErrorResponse(false, "VALIDATION_FAILED", message, domain, fieldMap, Instant.now());
    }
}
