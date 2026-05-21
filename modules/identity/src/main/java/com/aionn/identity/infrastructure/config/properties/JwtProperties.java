package com.aionn.identity.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for JWT signing and token lifetime.
 * <p>
 * {@code secret} MUST be overridden in production via
 * {@code IDENTITY_JWT_SECRET}
 * (min 32 chars for HS256).
 * <p>
 * {@code accessTokenExpiryMinutes} controls short-lived access token lifetime.
 * Default 15 min provides a good balance between security (quick revocation via
 * natural expiry) and UX (client refreshes transparently every ~14 min).
 */
@ConfigurationProperties(prefix = "identity.jwt")
public record JwtProperties(
                @DefaultValue("aionn-identity") String issuer,
                @DefaultValue("dev-only-secret-please-override-min-32-bytes!!") String secret,
                @DefaultValue("15") int accessTokenExpiryMinutes) {
}
