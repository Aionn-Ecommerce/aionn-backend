package com.aionn.shipping.adapter.rest.exception;

import com.aionn.shipping.domain.exception.ShippingException;
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
public class ShippingExceptionHandler {

    private static final Set<String> NOT_FOUND = Set.of("SHP_001", "SHP_101");
    private static final Set<String> CONFLICT = Set.of("SHP_003", "SHP_102");
    private static final Set<String> BAD_REQUEST = Set.of("SHP_002", "SHP_900");
    private static final Set<String> SERVICE_UNAVAILABLE = Set.of("SHP_004");

    @ExceptionHandler(ShippingException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handle(ShippingException ex) {
        log.warn("Shipping exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", ex.getErrorCode());
        body.put("domain", ex.getDomain());
        HttpStatus status = mapStatus(ex.getErrorCode());
        return ResponseEntity.status(status).body(
                ApiResponse.error(String.valueOf(status.value()), ex.getMessage(), body));
    }

    private HttpStatus mapStatus(String code) {
        if (code == null)
            return HttpStatus.UNPROCESSABLE_ENTITY;
        if (NOT_FOUND.contains(code))
            return HttpStatus.NOT_FOUND;
        if (CONFLICT.contains(code))
            return HttpStatus.CONFLICT;
        if (BAD_REQUEST.contains(code))
            return HttpStatus.BAD_REQUEST;
        if (SERVICE_UNAVAILABLE.contains(code))
            return HttpStatus.SERVICE_UNAVAILABLE;
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}

