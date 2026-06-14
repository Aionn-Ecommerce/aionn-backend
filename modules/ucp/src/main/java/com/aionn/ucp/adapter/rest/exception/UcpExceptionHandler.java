package com.aionn.ucp.adapter.rest.exception;

import com.aionn.sharedkernel.adapter.web.exception.AbstractModuleExceptionHandler;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import com.aionn.ucp.domain.exception.UcpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.aionn.ucp.adapter.rest.controller")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UcpExceptionHandler extends AbstractModuleExceptionHandler {

    public UcpExceptionHandler() {
        registerErrors(HttpStatus.BAD_REQUEST,
                "UCP_001", "UCP_101", "UCP_102", "UCP_201", "UCP_900");
        registerErrors(HttpStatus.UNPROCESSABLE_ENTITY,
                "UCP_003", "UCP_004");
        registerErrors(HttpStatus.FAILED_DEPENDENCY, "UCP_002");
        registerErrors(HttpStatus.NOT_FOUND,
                "UCP_103", "UCP_202", "UCP_301");
        registerErrors(HttpStatus.CONFLICT, "UCP_203");
        registerErrors(HttpStatus.FORBIDDEN, "UCP_204");
        setDefaultStatus(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Override
    protected String moduleDomain() {
        return "UCP";
    }

    @ExceptionHandler(UcpException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleUcpException(UcpException ex) {
        log.warn("UCP exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return handleException(ex);
    }
}
