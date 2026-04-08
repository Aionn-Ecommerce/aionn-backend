package com.ecommerce.identity.adapter.rest.exception;

import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.sharedkernel.adapter.web.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IdentityExceptionHandler {

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
        if (errorCode.contains("NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }
        if (errorCode.contains("ALREADY_EXISTS") || errorCode.contains("CONFLICT")) {
            return HttpStatus.CONFLICT;
        }
        if (errorCode.contains("UNAUTHORIZED") || errorCode.contains("INVALID_CREDENTIALS")) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (errorCode.contains("FORBIDDEN") || errorCode.contains("ACCESS_DENIED")) {
            return HttpStatus.FORBIDDEN;
        }
        if (errorCode.contains("INVALID") || errorCode.contains("VALIDATION")) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
