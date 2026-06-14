package com.aionn.ucp.domain.exception;

import com.aionn.sharedkernel.common.exception.DomainException;

public class UcpException extends DomainException {

    public UcpException(UcpErrorCode errorCode) {
        super("UCP", errorCode.getCode(), errorCode.getDefaultMessage());
    }

    public UcpException(UcpErrorCode errorCode, String customMessage) {
        super("UCP", errorCode.getCode(), customMessage);
    }

    public UcpException(UcpErrorCode errorCode, String customMessage, Throwable cause) {
        super("UCP", errorCode.getCode(), customMessage, cause);
    }
}
