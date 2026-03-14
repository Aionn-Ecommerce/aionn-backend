package com.ecommerce.sharedkernel.presentation.exception;

public class ConflictException extends DomainException {

    public ConflictException(String domain, String errorCode, String message) {
        super(domain, errorCode, message);
    }
}