package com.ecommerce.sharedkernel.domain.vo;

import com.ecommerce.sharedkernel.domain.model.ValueObject;
import java.util.Objects;
import java.util.regex.Pattern;

public record PhoneNumber(String value) implements ValueObject {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9][0-9\\s\\-]{6,14}[0-9]$");

    public PhoneNumber {
        Objects.requireNonNull(value, "PhoneNumber must not be null");
        value = normalize(value);

        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid phone number format: " + value);
        }
    }

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }

    public String toE164(String countryCode) {
        if ("VN".equalsIgnoreCase(countryCode) && value.startsWith("0")) {
            return "+84" + value.substring(1);
        }
        return value.startsWith("+") ? value : "+" + value;
    }

    public boolean isE164() {
        return value.startsWith("+");
    }

    private static String normalize(String raw) {
        return raw.trim().replaceAll("[\\s\\-]", "");
    }
}