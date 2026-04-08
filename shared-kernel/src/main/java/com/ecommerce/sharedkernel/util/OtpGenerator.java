package com.ecommerce.sharedkernel.util;

import java.util.concurrent.ThreadLocalRandom;

public final class OtpGenerator {

    private OtpGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String generateNumericOtp(int length) {
        if (length <= 0 || length > 10) {
            throw new IllegalArgumentException("OTP length must be between 1 and 10");
        }
        int max = (int) Math.pow(10, length);
        String format = "%0" + length + "d";
        return String.format(format, ThreadLocalRandom.current().nextInt(0, max));
    }

    public static String generate6DigitOtp() {
        return generateNumericOtp(6);
    }
}
