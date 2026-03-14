package com.ecommerce.sharedkernel.util;

import com.ecommerce.sharedkernel.presentation.exception.DomainException;
import com.ecommerce.sharedkernel.presentation.exception.ValidationException;

import java.util.Collection;
import java.util.function.Supplier;

public final class Preconditions {

    private Preconditions() {
    }

    public static <T> T requireNotNull(T value, String domain, String errorCode, String message) {
        if (value == null) {
            throw new DomainException(domain, errorCode, message);
        }
        return value;
    }

    public static <T> T requireNotNull(T value, String domain, String errorCode,
            Supplier<String> messageSupplier) {
        if (value == null) {
            throw new DomainException(domain, errorCode, messageSupplier.get());
        }
        return value;
    }

    public static String requireNotBlank(String value, String domain, String errorCode, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(domain, errorCode, message);
        }
        return value.trim();
    }

    public static String requireMaxLength(String value, int maxLength,
            String domain, String errorCode, String message) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationException(domain, errorCode, message);
        }
        return value;
    }

    public static int requirePositive(int value, String domain, String errorCode, String message) {
        if (value <= 0) {
            throw new ValidationException(domain, errorCode, message);
        }
        return value;
    }

    public static int requireNonNegative(int value, String domain, String errorCode, String message) {
        if (value < 0) {
            throw new ValidationException(domain, errorCode, message);
        }
        return value;
    }

    public static <T extends Comparable<T>> T requireMin(T value, T min,
            String domain, String errorCode, String message) {
        if (value.compareTo(min) < 0) {
            throw new ValidationException(domain, errorCode, message);
        }
        return value;
    }

    public static <T extends Comparable<T>> T requireRange(T value, T min, T max,
            String domain, String errorCode, String message) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new ValidationException(domain, errorCode, message);
        }
        return value;
    }

    public static <C extends Collection<?>> C requireNotEmpty(C collection,
            String domain, String errorCode, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new ValidationException(domain, errorCode, message);
        }
        return collection;
    }

    public static <C extends Collection<?>> C requireMaxSize(C collection, int maxSize,
            String domain, String errorCode, String message) {
        if (collection != null && collection.size() > maxSize) {
            throw new ValidationException(domain, errorCode, message);
        }
        return collection;
    }

    public static void requireTrue(boolean condition, String domain, String errorCode, String message) {
        if (!condition) {
            throw new DomainException(domain, errorCode, message);
        }
    }

    public static void requireFalse(boolean condition, String domain, String errorCode, String message) {
        if (condition) {
            throw new DomainException(domain, errorCode, message);
        }
    }

    public static void requireState(boolean condition, String domain, String errorCode, String message) {
        requireTrue(condition, domain, errorCode, message);
    }
}
