package com.aionn.catalog.adapter.rest.exception;

import com.aionn.catalog.domain.exception.CatalogException;
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
public class CatalogExceptionHandler {

    private static final Set<String> NOT_FOUND_CODES = Set.of(
            "CATALOG_001", // MERCHANT_NOT_FOUND
            "CATALOG_101", // CATEGORY_NOT_FOUND
            "CATALOG_201", // BRAND_NOT_FOUND
            "CATALOG_301", // PRODUCT_NOT_FOUND
            "CATALOG_305", // PRODUCT_VARIANT_NOT_FOUND
            "CATALOG_401"); // ATTRIBUTE_TEMPLATE_NOT_FOUND

    private static final Set<String> CONFLICT_CODES = Set.of(
            "CATALOG_002", // MERCHANT_ALREADY_EXISTS
            "CATALOG_004", // MERCHANT_HAS_OPEN_ORDERS
            "CATALOG_102", // CATEGORY_NAME_CONFLICT
            "CATALOG_103", // CATEGORY_SLUG_CONFLICT
            "CATALOG_104", // CATEGORY_HAS_PRODUCTS
            "CATALOG_202", // BRAND_NAME_CONFLICT
            "CATALOG_203", // BRAND_HAS_ACTIVE_PRODUCTS
            "CATALOG_306"); // PRODUCT_VARIANT_DUPLICATE

    private static final Set<String> FORBIDDEN_CODES = Set.of(
            "CATALOG_005", // MERCHANT_FORBIDDEN
            "CATALOG_303"); // PRODUCT_FORBIDDEN

    private static final Set<String> BAD_REQUEST_CODES = Set.of(
            "CATALOG_003", // MERCHANT_INVALID_TRANSITION
            "CATALOG_105", // CATEGORY_CYCLE
            "CATALOG_302", // PRODUCT_INVALID_TRANSITION
            "CATALOG_304", // PRODUCT_PUBLISH_REQUIREMENTS
            "CATALOG_307", // PRODUCT_BRAND_NOT_APPROVED
            "CATALOG_308", // PRODUCT_CATEGORY_REQUIRED
            "CATALOG_309", // PRODUCT_BULK_TOO_LARGE
            "CATALOG_402", // ATTRIBUTE_KEY_NOT_FOUND
            "CATALOG_900"); // INVALID_ARGUMENT

    @ExceptionHandler(CatalogException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleCatalogException(CatalogException ex) {
        log.warn("Catalog exception [{}]: {}", ex.getErrorCode(), ex.getMessage());

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
        if (FORBIDDEN_CODES.contains(errorCode)) {
            return HttpStatus.FORBIDDEN;
        }
        if (BAD_REQUEST_CODES.contains(errorCode)) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}

