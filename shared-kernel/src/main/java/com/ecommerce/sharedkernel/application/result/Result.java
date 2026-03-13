package com.ecommerce.sharedkernel.application.result;

public record Result<T>(T value, String error, boolean isSuccess) {
    public Result {
        if (isSuccess && error != null && !error.isBlank()) {
            throw new IllegalArgumentException("Successful result must not contain an error.");
        }
        if (!isSuccess && (error == null || error.isBlank())) {
            throw new IllegalArgumentException("Failed result must contain an error.");
        }
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null, true);
    }

    public static <T> Result<T> failure(String error) {
        return new Result<>(null, error, false);
    }
}