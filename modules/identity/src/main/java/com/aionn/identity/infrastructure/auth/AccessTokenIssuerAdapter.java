package com.aionn.identity.infrastructure.auth;

import com.aionn.identity.application.port.out.auth.AccessTokenIssuer;
import com.aionn.identity.infrastructure.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Short-lived HMAC-signed JWT access token (Option B architecture).
 * <p>
 * Access tokens expire after {@link JwtProperties#accessTokenExpiryMinutes()}
 * (default 15 min). They are self-contained: the bearer filter trusts the JWT
 * signature + expiry without querying the session DB. Emergency revocation is
 * handled via a Redis token blacklist keyed by {@code jti}.
 * <p>
 * The shared secret comes from {@link JwtProperties} and must be at least
 * 32 bytes for HS256.
 */
@Component
@RequiredArgsConstructor
public class AccessTokenIssuerAdapter implements AccessTokenIssuer {

    private final JwtProperties jwtProperties;

    @Override
    public String issueAccessToken(String userId, String sessionId, LocalDateTime expiresAt, Set<String> roles) {
        // Access token expiry is short-lived (15 min by default), independent of
        // session expiry.
        // Session expiry is only relevant for refresh token rotation.
        Instant now = Instant.now();
        Instant accessExpiry = now.plusSeconds(jwtProperties.accessTokenExpiryMinutes() * 60L);

        // Cap to session expiry if session ends sooner than access token lifetime
        Instant sessionExpiry = expiresAt.atZone(ZoneId.systemDefault()).toInstant();
        if (accessExpiry.isAfter(sessionExpiry)) {
            accessExpiry = sessionExpiry;
        }

        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // jti for blacklist support
                .subject(userId)
                .claim("sid", sessionId)
                .claim("roles", roles != null ? roles : Set.of())
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessExpiry))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Parse and validate a token. Returns the parsed claims if valid, or empty
     * if the signature/expiry/structure is invalid.
     */
    public Optional<Claims> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .requireIssuer(jwtProperties.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private SecretKey signingKey() {
        byte[] bytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}
