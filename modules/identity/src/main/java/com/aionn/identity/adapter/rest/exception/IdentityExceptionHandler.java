package com.aionn.identity.adapter.rest.exception;

import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Translates {@link IdentityException} to HTTP status codes. Status mapping is
 * driven by an explicit table rather than substring matching of the error
 * code, so codes like {@code KYC_INVALID_STATUS_TRANSITION} map to a
 * meaningful HTTP code instead of being lumped into 400.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IdentityExceptionHandler {

    private static final Set<String> NOT_FOUND_CODES = Set.of(
            "IDENTITY_003", "IDENTITY_109", "IDENTITY_201", "IDENTITY_205",
            "IDENTITY_208", "IDENTITY_215", "IDENTITY_217", "IDENTITY_218",
            "IDENTITY_219", "IDENTITY_302", "IDENTITY_401", "IDENTITY_501",
            "IDENTITY_601");

    private static final Set<String> CONFLICT_CODES = Set.of(
            "IDENTITY_001", "IDENTITY_002", "IDENTITY_005", "IDENTITY_112",
            "IDENTITY_207", "IDENTITY_214", "IDENTITY_216");

    private static final Set<String> UNAUTHORIZED_CODES = Set.of(
            "IDENTITY_106", "IDENTITY_203", "IDENTITY_210");

    private static final Set<String> FORBIDDEN_CODES = Set.of(
            "IDENTITY_206", "IDENTITY_220");

    private static final Set<String> BAD_REQUEST_CODES = Set.of(
            "IDENTITY_006", "IDENTITY_101", "IDENTITY_104", "IDENTITY_105",
            "IDENTITY_202", "IDENTITY_211", "IDENTITY_212", "IDENTITY_213",
            "IDENTITY_301", "IDENTITY_304", "IDENTITY_602");

    private static final Set<String> TOO_MANY_REQUESTS_CODES = Set.of("IDENTITY_107");

    @ExceptionHandler(IdentityException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleIdentityException(IdentityException ex) {
        log.warn("Identity exception [{}]: {}", ex.getErrorCode(), ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", ex.getErrorCode());
        body.put("domain", ex.getDomain());

        HttpStatus status = mapErrorCodeToHttpStatus(ex.getErrorCode());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(String.valueOf(status.value()), ex.getMessage(), body));
    }

    private HttpStatus mapErrorCodeToHttpStatus(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }
        if (NOT_FOUND_CODES.contains(errorCode)) {
            return HttpStatus.NOT_FOUND;
        }
        if (CONFLICT_CODES.contains(errorCode)) {
            return HttpStatus.CONFLICT;
        }
        if (UNAUTHORIZED_CODES.contains(errorCode)) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (FORBIDDEN_CODES.contains(errorCode)) {
            return HttpStatus.FORBIDDEN;
        }
        if (BAD_REQUEST_CODES.contains(errorCode)) {
            return HttpStatus.BAD_REQUEST;
        }
        if (TOO_MANY_REQUESTS_CODES.contains(errorCode)) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}

