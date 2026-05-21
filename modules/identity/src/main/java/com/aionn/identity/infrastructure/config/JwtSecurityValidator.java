package com.aionn.identity.infrastructure.config;

import com.aionn.identity.infrastructure.config.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Validates that JWT secrets are not using dev-default values in production.
 * Fails fast at startup to prevent insecure deployments.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSecurityValidator {

    private static final String DEV_SECRET = "dev-only-secret-please-override-min-32-bytes!!";
    private static final String DEV_JWT_SECRET = "dev-jwt-secret-please-override-and-use-min-32-bytes!!";
    private static final int MIN_SECRET_LENGTH = 32;

    private final JwtProperties jwtProperties;
    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void validateJwtSecurity() {
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod")
                || Arrays.asList(environment.getActiveProfiles()).contains("production");

        String secret = jwtProperties.secret();

        if (isProd) {
            if (DEV_SECRET.equals(secret) || DEV_JWT_SECRET.equals(secret)) {
                throw new IllegalStateException(
                        "CRITICAL: JWT secret is using dev-default value in production! " +
                                "Set IDENTITY_JWT_SECRET environment variable with a secure random string (min 32 chars).");
            }
            if (secret.length() < MIN_SECRET_LENGTH) {
                throw new IllegalStateException(
                        "CRITICAL: JWT secret must be at least " + MIN_SECRET_LENGTH +
                                " characters for HS256. Current length: " + secret.length());
            }
            log.info("JWT security validation passed for production profile");
        } else {
            if (DEV_SECRET.equals(secret) || DEV_JWT_SECRET.equals(secret)) {
                log.warn(
                        "JWT secret is using dev-default value. Override via IDENTITY_JWT_SECRET for non-dev environments.");
            }
        }
    }
}
