package com.aionn.chat.adapter.rest.exception;

import com.aionn.chat.domain.exception.ChatException;
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
public class ChatExceptionHandler {

    private static final Set<String> NOT_FOUND = Set.of(
            "CHT_001", "CHT_101", "CHT_202", "CHT_301");

    private static final Set<String> FORBIDDEN = Set.of(
            "CHT_002", "CHT_102");

    private static final Set<String> CONFLICT = Set.of(
            "CHT_201");

    private static final Set<String> BAD_REQUEST = Set.of(
            "CHT_003", "CHT_103", "CHT_104", "CHT_105", "CHT_106", "CHT_203", "CHT_900");

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handle(ChatException ex) {
        log.warn("Chat exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", ex.getErrorCode());
        body.put("domain", ex.getDomain());

        HttpStatus status = mapStatus(ex.getErrorCode());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(String.valueOf(status.value()), ex.getMessage(), body));
    }

    private static HttpStatus mapStatus(String code) {
        if (code == null)
            return HttpStatus.UNPROCESSABLE_ENTITY;
        if (NOT_FOUND.contains(code))
            return HttpStatus.NOT_FOUND;
        if (FORBIDDEN.contains(code))
            return HttpStatus.FORBIDDEN;
        if (CONFLICT.contains(code))
            return HttpStatus.CONFLICT;
        if (BAD_REQUEST.contains(code))
            return HttpStatus.BAD_REQUEST;
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}

