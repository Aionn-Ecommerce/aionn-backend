package com.ecommerce.sharedkernel.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    public Email {
        Objects.requireNonNull(value, "Email must not be null");
        value = value.trim().toLowerCase();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }

    public String getDomain() {
        return value.substring(value.indexOf('@') + 1);
    }

    public String getLocalPart() {
        return value.substring(0, value.indexOf('@'));
    }
}
