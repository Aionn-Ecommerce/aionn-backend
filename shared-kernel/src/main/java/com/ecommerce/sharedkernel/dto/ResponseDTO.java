package com.ecommerce.sharedkernel.dto;

public record ResponseDTO<T>(boolean success, String message, T data, String errorCode) {
    public ResponseDTO {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }

    public static <T> ResponseDTO<T> ok(T data) {
        return new ResponseDTO<>(true, "Success", data, null);
    }

    public static <T> ResponseDTO<T> fail(String message, String errorCode) {
        return new ResponseDTO<>(false, message, null, errorCode);
    }
}