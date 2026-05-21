package com.aionn.promotion.adapter.rest.exception;

import com.aionn.promotion.domain.exception.PromotionException;
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
public class PromotionExceptionHandler {

    private static final Set<String> NOT_FOUND = Set.of("PRM_001", "PRM_101", "PRM_203");
    private static final Set<String> CONFLICT = Set.of(
            "PRM_103", "PRM_104", "PRM_201", "PRM_202", "PRM_205");
    private static final Set<String> BAD_REQUEST = Set.of(
            "PRM_002", "PRM_003", "PRM_004", "PRM_102", "PRM_204",
            "PRM_301", "PRM_302", "PRM_900");

    @ExceptionHandler(PromotionException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handle(PromotionException ex) {
        log.warn("Promotion exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
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
        if (BAD_REQUEST.contains(code))
            return HttpStatus.BAD_REQUEST;
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}

