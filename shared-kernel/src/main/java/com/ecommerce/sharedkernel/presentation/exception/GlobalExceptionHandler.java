package com.ecommerce.sharedkernel.presentation.exception;

import com.ecommerce.sharedkernel.common.exception.ConflictException;
import com.ecommerce.sharedkernel.common.exception.DomainException;
import com.ecommerce.sharedkernel.common.exception.ForbiddenException;
import com.ecommerce.sharedkernel.common.exception.NotFoundException;
import com.ecommerce.sharedkernel.common.exception.UnauthorizedException;
import com.ecommerce.sharedkernel.common.exception.ValidationException;
import com.ecommerce.sharedkernel.presentation.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
                log.debug("Not found: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), ex.getDomain()));
        }

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
                log.debug("Validation failed: {}", ex.getMessage());
                if (!ex.getFieldErrors().isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(ErrorResponse.withFieldErrors(ex.getMessage(), ex.getDomain(),
                                                        ex.getFieldErrors()));
                }
                return ResponseEntity.badRequest()
                                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), ex.getDomain()));
        }

        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
                log.debug("Conflict: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), ex.getDomain()));
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
                log.debug("Unauthorized: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
        }

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
                log.debug("Forbidden: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), ex.getDomain()));
        }

        @ExceptionHandler(DomainException.class)
        public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
                log.warn("Domain exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
                return ResponseEntity.unprocessableEntity()
                                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), ex.getDomain()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex) {
                List<ValidationException.FieldError> fieldErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(fe -> new ValidationException.FieldError(fe.getField(),
                                                fe.getDefaultMessage()))
                                .toList();

                log.debug("Request validation failed: {} field error(s)", fieldErrors.size());
                return ResponseEntity.badRequest()
                                .body(ErrorResponse.withFieldErrors("Request validation failed", "Request",
                                                fieldErrors));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(
                        MethodArgumentTypeMismatchException ex) {
                String message = "Invalid value '%s' for parameter '%s'".formatted(ex.getValue(), ex.getName());
                return ResponseEntity.badRequest()
                                .body(ErrorResponse.of("INVALID_PARAMETER", message));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
                log.error("Unexpected error", ex);
                return ResponseEntity.internalServerError()
                                .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred"));
        }
}
