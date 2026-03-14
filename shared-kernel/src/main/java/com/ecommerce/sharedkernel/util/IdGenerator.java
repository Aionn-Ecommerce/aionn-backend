package com.ecommerce.sharedkernel.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

public final class IdGenerator {

    private IdGenerator() {
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final char[] ENCODING = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    public static UUID uuid() {
        return UUID.randomUUID();
    }

    public static UUID parse(String id) {
        return UUID.fromString(id);
    }

    public static UUID parseOrNull(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String ulid() {
        return ulid(Instant.now().toEpochMilli());
    }

    public static String ulid(long timestampMs) {
        char[] ulid = new char[26];

        // 10 chars of timestamp (48 bits)
        ulid[0] = ENCODING[(int) ((timestampMs >>> 45) & 0x1F)];
        ulid[1] = ENCODING[(int) ((timestampMs >>> 40) & 0x1F)];
        ulid[2] = ENCODING[(int) ((timestampMs >>> 35) & 0x1F)];
        ulid[3] = ENCODING[(int) ((timestampMs >>> 30) & 0x1F)];
        ulid[4] = ENCODING[(int) ((timestampMs >>> 25) & 0x1F)];
        ulid[5] = ENCODING[(int) ((timestampMs >>> 20) & 0x1F)];
        ulid[6] = ENCODING[(int) ((timestampMs >>> 15) & 0x1F)];
        ulid[7] = ENCODING[(int) ((timestampMs >>> 10) & 0x1F)];
        ulid[8] = ENCODING[(int) ((timestampMs >>> 5) & 0x1F)];
        ulid[9] = ENCODING[(int) ((timestampMs) & 0x1F)];

        // 16 chars of randomness (80 bits)
        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);
        ulid[10] = ENCODING[(randomBytes[0] & 0xFF) >>> 3];
        ulid[11] = ENCODING[((randomBytes[0] & 0x07) << 2) | ((randomBytes[1] & 0xFF) >>> 6)];
        ulid[12] = ENCODING[(randomBytes[1] & 0x3E) >>> 1];
        ulid[13] = ENCODING[((randomBytes[1] & 0x01) << 4) | ((randomBytes[2] & 0xFF) >>> 4)];
        ulid[14] = ENCODING[((randomBytes[2] & 0x0F) << 1) | ((randomBytes[3] & 0xFF) >>> 7)];
        ulid[15] = ENCODING[(randomBytes[3] & 0x7C) >>> 2];
        ulid[16] = ENCODING[((randomBytes[3] & 0x03) << 3) | ((randomBytes[4] & 0xFF) >>> 5)];
        ulid[17] = ENCODING[randomBytes[4] & 0x1F];
        ulid[18] = ENCODING[(randomBytes[5] & 0xFF) >>> 3];
        ulid[19] = ENCODING[((randomBytes[5] & 0x07) << 2) | ((randomBytes[6] & 0xFF) >>> 6)];
        ulid[20] = ENCODING[(randomBytes[6] & 0x3E) >>> 1];
        ulid[21] = ENCODING[((randomBytes[6] & 0x01) << 4) | ((randomBytes[7] & 0xFF) >>> 4)];
        ulid[22] = ENCODING[((randomBytes[7] & 0x0F) << 1) | ((randomBytes[8] & 0xFF) >>> 7)];
        ulid[23] = ENCODING[(randomBytes[8] & 0x7C) >>> 2];
        ulid[24] = ENCODING[((randomBytes[8] & 0x03) << 3) | ((randomBytes[9] & 0xFF) >>> 5)];
        ulid[25] = ENCODING[randomBytes[9] & 0x1F];

        return new String(ulid);
    }

    public static UUID ulidAsUuid() {
        String ulid = ulid();
        long high = 0, low = 0;
        for (int i = 0; i < 13; i++) {
            high = (high << 5) | indexOf(ulid.charAt(i));
        }
        for (int i = 13; i < 26; i++) {
            low = (low << 5) | indexOf(ulid.charAt(i));
        }
        high = (high >>> 2);
        low = (low & 0x3FFFFFFFFFFFFFFFL) | ((high & 0x3L) << 62);
        high = high >>> 2;
        return new UUID(high, low);
    }

    private static int indexOf(char c) {
        for (int i = 0; i < ENCODING.length; i++) {
            if (ENCODING[i] == c)
                return i;
        }
        throw new IllegalArgumentException("Invalid ULID character: " + c);
    }
}
