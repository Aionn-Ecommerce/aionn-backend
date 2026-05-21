package com.aionn.inventory.adapter.rest.exception;

import com.aionn.inventory.domain.exception.InventoryException;
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
public class InventoryExceptionHandler {

    private static final Set<String> NOT_FOUND = Set.of(
            "INV_001", // WAREHOUSE_NOT_FOUND
            "INV_101", // INVENTORY_ITEM_NOT_FOUND
            "INV_201", // STOCK_TRANSFER_NOT_FOUND
            "INV_301" // STOCK_RESERVATION_NOT_FOUND
    );

    private static final Set<String> CONFLICT = Set.of(
            "INV_102", // ALREADY_INITIALIZED
            "INV_105" // INVENTORY_LOCKED
    );

    private static final Set<String> FORBIDDEN = Set.of(
            "INV_002", // WAREHOUSE_FORBIDDEN
            "INV_204" // STOCK_TRANSFER_DIFFERENT_MERCHANT
    );

    private static final Set<String> BAD_REQUEST = Set.of(
            "INV_003", "INV_103", "INV_104", "INV_106", "INV_107",
            "INV_202", "INV_203",
            "INV_302",
            "INV_401",
            "INV_900");

    @ExceptionHandler(InventoryException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleInventoryException(InventoryException ex) {
        log.warn("Inventory exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
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

