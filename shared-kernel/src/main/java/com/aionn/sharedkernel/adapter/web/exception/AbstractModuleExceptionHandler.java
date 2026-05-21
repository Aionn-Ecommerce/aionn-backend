package com.aionn.sharedkernel.adapter.web.exception;

import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import com.aionn.sharedkernel.common.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

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
}
