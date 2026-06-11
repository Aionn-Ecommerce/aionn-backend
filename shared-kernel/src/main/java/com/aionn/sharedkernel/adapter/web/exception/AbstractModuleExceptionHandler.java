package com.aionn.sharedkernel.adapter.web.exception;

import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import com.aionn.sharedkernel.common.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base class for per-module REST exception handlers.
 *
 * <p>
 * Concrete subclasses are annotated with
 * {@code @RestControllerAdvice(basePackages = "<module>.adapter.rest.controller")}
 * so each module owns its own scope. Subclasses register their domain error
 * codes via {@link #registerErrors(HttpStatus, String...)} in the constructor
 * and add a single {@code @ExceptionHandler(<X>Exception.class)} that delegates
 * to {@link #handleException(DomainException)}.
 *
 * <p>
 * Cross-cutting exceptions (validation, authentication, malformed body…)
 * are handled here once and inherited by every module advice, so the public
 * error envelope is consistent across the API.
 */
public abstract class AbstractModuleExceptionHandler {

    private final Map<String, HttpStatus> codeToStatus = new HashMap<>();
    private HttpStatus defaultStatus = HttpStatus.UNPROCESSABLE_ENTITY;

    protected void registerErrors(HttpStatus status, String... errorCodes) {
        for (String code : errorCodes) {
            if (code != null) {
                codeToStatus.put(code, status);
            }
        }
    }

    protected void setDefaultStatus(HttpStatus status) {
        this.defaultStatus = status;
    }

    /**
     * Domain label rendered into the {@code domain} response field for
     * non-{@link DomainException} errors (validation, auth, etc.). Subclasses
     * should override to return their module name (e.g. {@code "Catalog"}).
     */
    protected String moduleDomain() {
        return "Unknown";
    }

    protected ResponseEntity<ApiResponse<Map<String, Object>>> handleException(DomainException ex) {
        HttpStatus status = resolveStatus(ex.getErrorCode());
        Map<String, Object> body = Map.of(
                "errorCode", ex.getErrorCode() != null ? ex.getErrorCode() : "UNKNOWN",
                "domain", ex.getDomain() != null ? ex.getDomain() : "UNKNOWN");
        return ResponseEntity.status(status)
                .body(ApiResponse.error(String.valueOf(status.value()), ex.getMessage(), body));
    }

    protected HttpStatus resolveStatus(String errorCode) {
        if (errorCode == null) {
            return defaultStatus;
        }
        return codeToStatus.getOrDefault(errorCode, defaultStatus);
    }

    // ─── Cross-cutting handlers shared by every module ─────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (a, b) -> a + "; " + b,
                        LinkedHashMap::new));
        return buildError(HttpStatus.BAD_REQUEST, "Request validation failed", "VALIDATION_FAILED", fieldErrors);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleAuthentication(AuthenticationException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "Authentication required", "AUTHENTICATION_REQUIRED", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleAccessDenied(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, "Access denied", "ACCESS_DENIED", null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value '%s' for parameter '%s'".formatted(ex.getValue(), ex.getName());
        return buildError(HttpStatus.BAD_REQUEST, message, "INVALID_PARAMETER", null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleNotReadable(HttpMessageNotReadableException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Malformed JSON request body", "MALFORMED_BODY", null);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMissingHeader(MissingRequestHeaderException ex) {
        return buildError(HttpStatus.BAD_REQUEST,
                "Missing required header: " + ex.getHeaderName(),
                "MISSING_HEADER",
                null);
    }

    protected ResponseEntity<ApiResponse<Map<String, Object>>> buildError(
            HttpStatus status,
            String message,
            String errorCode,
            Map<String, String> fieldErrors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", errorCode);
        body.put("domain", moduleDomain());
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            body.put("fieldErrors", fieldErrors);
        }
        return ResponseEntity.status(status)
                .body(ApiResponse.error(String.valueOf(status.value()), message, body));
    }
}
