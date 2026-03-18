package com.ecommerce.identity.infrastructure.registration;

import com.ecommerce.identity.application.port.out.registration.RegistrationRateLimiter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRegistrationRateLimiter implements RegistrationRateLimiter {

    private final Map<String, ArrayDeque<Long>> requestsByKey = new ConcurrentHashMap<>();

    @Override
    public boolean check(String scope, String key, int maxAttempts, int windowSeconds) {
        if (key == null || key.isBlank()) {
            return true;
        }

        String bucket = scope + ":" + key;
        long now = Instant.now().getEpochSecond();
        long threshold = now - windowSeconds;

        ArrayDeque<Long> queue = requestsByKey.computeIfAbsent(bucket, k -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && queue.peekFirst() <= threshold) {
                queue.pollFirst();
            }
            if (queue.size() >= maxAttempts) {
                return false;
            }
            queue.addLast(now);
            return true;
        }
    }
}
