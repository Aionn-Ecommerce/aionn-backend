package com.aionn.payment.adapter.rest.exception;

import com.aionn.payment.domain.exception.PaymentException;
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

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PaymentExceptionHandler {

    private static final Set<String> NOT_FOUND = Set.of("PAY_001", "PAY_101", "PAY_201");
    private static final Set<String> CONFLICT = Set.of("PAY_003");
    private static final Set<String> FORBIDDEN = Set.of("PAY_102");
    private static final Set<String> BAD_REQUEST = Set.of(
            "PAY_002", "PAY_005", "PAY_006", "PAY_103", "PAY_900");
    private static final Set<String> SERVICE_UNAVAILABLE = Set.of("PAY_004");

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handle(PaymentException ex) {
        log.warn("Payment exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", ex.getErrorCode());
        body.put("domain", ex.getDomain());
        HttpStatus status = mapStatus(ex.getErrorCode());
        return ResponseEntity.status(status).body(ApiResponse.error(
                String.valueOf(status.value()), ex.getMessage(), body));
    }

    private HttpStatus mapStatus(String code) {
        if (code == null)
            return HttpStatus.UNPROCESSABLE_ENTITY;
        if (NOT_FOUND.contains(code))
            return HttpStatus.NOT_FOUND;
        if (CONFLICT.contains(code))
            return HttpStatus.CONFLICT;
        if (FORBIDDEN.contains(code))
            return HttpStatus.FORBIDDEN;
        if (BAD_REQUEST.contains(code))
            return HttpStatus.BAD_REQUEST;
        if (SERVICE_UNAVAILABLE.contains(code))
            return HttpStatus.SERVICE_UNAVAILABLE;
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}

