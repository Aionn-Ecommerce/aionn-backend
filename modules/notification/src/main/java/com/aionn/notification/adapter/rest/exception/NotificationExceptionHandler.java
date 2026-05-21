package com.aionn.notification.adapter.rest.exception;

import com.aionn.notification.domain.exception.NotificationException;
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
public class NotificationExceptionHandler {

    private static final Set<String> NOT_FOUND = Set.of(
            "NTF_001", "NTF_101", "NTF_201", "NTF_301", "NTF_401");
    private static final Set<String> CONFLICT = Set.of("NTF_102");
    private static final Set<String> FORBIDDEN = Set.of("NTF_002");
    private static final Set<String> BAD_REQUEST = Set.of(
            "NTF_003", "NTF_103", "NTF_202", "NTF_302", "NTF_900");

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handle(NotificationException ex) {
        log.warn("Notification exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
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
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}

