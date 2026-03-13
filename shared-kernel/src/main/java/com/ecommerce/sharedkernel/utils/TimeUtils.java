package com.ecommerce.sharedkernel.utils;

import java.time.Clock;
import java.time.Instant;

public final class TimeUtils {
    private static volatile Clock clock = Clock.systemUTC();

    private TimeUtils() {
    }

    public static Instant now() {
        return Instant.now(clock);
    }

    public static void useClock(Clock customClock) {
        clock = customClock;
    }

    public static void resetClock() {
        clock = Clock.systemUTC();
    }
}
