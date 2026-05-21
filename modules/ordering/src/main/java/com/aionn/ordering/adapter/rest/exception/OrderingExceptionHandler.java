package com.aionn.ordering.adapter.rest.exception;

import com.aionn.ordering.domain.exception.OrderingException;
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
public class OrderingExceptionHandler {

    private static final Set<String> NOT_FOUND = Set.of(
            "ORD_001", "ORD_003", "ORD_101", "ORD_201");

    private static final Set<String> CONFLICT = Set.of(
            "ORD_104");

    private static final Set<String> FORBIDDEN = Set.of(
            "ORD_002", "ORD_102", "ORD_106");

    private static final Set<String> BAD_REQUEST = Set.of(
            "ORD_004", "ORD_103", "ORD_105", "ORD_107", "ORD_108", "ORD_202", "ORD_900");

    @ExceptionHandler(OrderingException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handle(OrderingException ex) {
        log.warn("Ordering exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", ex.getErrorCode());
        body.put("domain", ex.getDomain());
        HttpStatus status = mapStatus(ex.getErrorCode());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(String.valueOf(status.value()), ex.getMessage(), body));
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
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}

