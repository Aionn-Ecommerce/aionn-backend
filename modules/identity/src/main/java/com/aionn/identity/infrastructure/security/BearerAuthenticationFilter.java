package com.aionn.identity.infrastructure.security;

import com.aionn.identity.application.port.out.auth.TokenBlacklist;
import com.aionn.identity.infrastructure.auth.AccessTokenIssuerAdapter;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Stateless JWT authentication filter (Option B architecture).
 * <p>
 * This filter trusts the JWT signature and expiry without querying the session
 * database on every request. This eliminates 2 DB calls per request and makes
 * the filter microservice-ready — any service with the signing key can verify
 * tokens independently.
 * <p>
 * Emergency revocation (logout, ban) is handled via a Redis token blacklist
 * checked by {@code jti}. The blacklist entries auto-expire when the token
 * naturally expires.
 * <p>
 * Flow:
 * <ol>
 * <li>Extract Bearer token from Authorization header</li>
 * <li>Verify JWT signature + expiry (rejects expired/tampered tokens)</li>
 * <li>Check token blacklist (rejects revoked tokens)</li>
 * <li>Extract roles from claims and set SecurityContext</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BearerAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final AccessTokenIssuerAdapter tokenIssuer;
    private final TokenBlacklist tokenBlacklist;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIX.length()).trim();
        Optional<Claims> parsed = tokenIssuer.parse(token);
        if (parsed.isEmpty()) {
            log.debug("Bearer token failed signature/expiry validation");
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = parsed.get();
        String userId = claims.getSubject();
        String sessionId = claims.get("sid", String.class);
        String jti = claims.getId();

        if (userId == null || sessionId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check token blacklist (logout/ban emergency revocation)
        if (jti != null && tokenBlacklist.isBlacklisted(jti)) {
            log.debug("Bearer token jti={} is blacklisted", jti);
            filterChain.doFilter(request, response);
            return;
        }

        // Extract roles from JWT claims — self-contained authorization
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        Collection<? extends GrantedAuthority> authorities;
        if (roles != null && !roles.isEmpty()) {
            authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        } else {
            authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        var authentication = new UsernamePasswordAuthenticationToken(userId, token, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setAttribute("identity.session.id", sessionId);

        filterChain.doFilter(request, response);
    }
}
