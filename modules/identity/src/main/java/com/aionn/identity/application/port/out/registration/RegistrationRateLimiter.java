package com.aionn.identity.application.port.out.registration;

public interface RegistrationRateLimiter {

    boolean check(String scope, String key, int maxAttempts, int windowSeconds);
}


