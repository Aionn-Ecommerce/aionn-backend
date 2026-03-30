package com.ecommerce.sharedkernel.adapter.web.exception;

import com.ecommerce.sharedkernel.common.exception.ConflictException;
import com.ecommerce.sharedkernel.common.exception.DomainException;
import com.ecommerce.sharedkernel.common.exception.ForbiddenException;
import com.ecommerce.sharedkernel.common.exception.NotFoundException;
import com.ecommerce.sharedkernel.common.exception.UnauthorizedException;
import com.ecommerce.sharedkernel.common.exception.ValidationException;
import com.ecommerce.sharedkernel.adapter.web.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ApiResponse<Map<String, Object>>> handleNotFound(NotFoundException ex) {
                log.debug("Not found: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(errorBody(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getErrorCode(), ex.getDomain(),
                                                null));
        }

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidation(ValidationException ex) {
                log.debug("Validation failed: {}", ex.getMessage());
                if (!ex.getFieldErrors().isEmpty()) {
                        Map<String, String> fieldMap = ex.getFieldErrors().stream()
                                        .collect(java.util.stream.Collectors.toMap(
                                                        ValidationException.FieldError::field,
                                                        ValidationException.FieldError::message,
                                                        (a, b) -> a + "; " + b));
                        return ResponseEntity.badRequest()
                                        .body(errorBody(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrorCode(),
                                                        ex.getDomain(), fieldMap));
                }
                return ResponseEntity.badRequest()
                                .body(errorBody(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrorCode(),
                                                ex.getDomain(), null));
        }

        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ApiResponse<Map<String, Object>>> handleConflict(ConflictException ex) {
                log.debug("Conflict: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(errorBody(HttpStatus.CONFLICT, ex.getMessage(), ex.getErrorCode(), ex.getDomain(),
                                                null));
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ApiResponse<Map<String, Object>>> handleUnauthorized(UnauthorizedException ex) {
                log.debug("Unauthorized: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(errorBody(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex.getErrorCode(), null,
                                                null));
        }

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ApiResponse<Map<String, Object>>> handleForbidden(ForbiddenException ex) {
                log.debug("Forbidden: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(errorBody(HttpStatus.FORBIDDEN, ex.getMessage(), ex.getErrorCode(),
                                                ex.getDomain(), null));
        }

        @ExceptionHandler(DomainException.class)
        public ResponseEntity<ApiResponse<Map<String, Object>>> handleDomain(DomainException ex) {
                log.warn("Domain exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
                return ResponseEntity.unprocessableEntity()
                                .body(errorBody(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), ex.getErrorCode(),
                                                ex.getDomain(), null));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, Object>>> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex) {
                List<ValidationException.FieldError> fieldErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(fe -> new ValidationException.FieldError(fe.getField(),
                                                fe.getDefaultMessage()))
                                .toList();

                log.debug("Request validation failed: {} field error(s)", fieldErrors.size());
                Map<String, String> fieldMap = fieldErrors.stream()
                                .collect(java.util.stream.Collectors.toMap(
                                                ValidationException.FieldError::field,
                                                ValidationException.FieldError::message,
                                                (a, b) -> a + "; " + b));
                return ResponseEntity.badRequest()
                                .body(errorBody(HttpStatus.BAD_REQUEST, "Request validation failed",
                                                "VALIDATION_FAILED", "Request", fieldMap));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<Map<String, Object>>> handleTypeMismatch(
                        MethodArgumentTypeMismatchException ex) {
                String message = "Invalid value '%s' for parameter '%s'".formatted(ex.getValue(), ex.getName());
                return ResponseEntity.badRequest()
                                .body(errorBody(HttpStatus.BAD_REQUEST, message, "INVALID_PARAMETER", null, null));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Map<String, Object>>> handleUnexpected(Exception ex) {
                log.error("Unexpected error", ex);
                return ResponseEntity.internalServerError()
                                .body(errorBody(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred",
                                                "INTERNAL_ERROR", null, null));
        }

        private ApiResponse<Map<String, Object>> errorBody(
                        HttpStatus status,
                        String message,
                        String errorCode,
                        String domain,
                        Map<String, String> fieldErrors) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("errorCode", errorCode);
                if (domain != null) {
                        body.put("domain", domain);
                }
                if (fieldErrors != null && !fieldErrors.isEmpty()) {
                        body.put("fieldErrors", fieldErrors);
                }
                return ApiResponse.error(String.valueOf(status.value()), message, body);
        }
}
