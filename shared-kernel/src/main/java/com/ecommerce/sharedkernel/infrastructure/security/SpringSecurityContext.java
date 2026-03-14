package com.ecommerce.sharedkernel.infrastructure.security;

import com.ecommerce.sharedkernel.presentation.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SpringSecurityContext implements SecurityContext {

    @Override
    public Optional<UUID> getCurrentUserId() {
        return getAuthentication()
                .map(auth -> parseUserId(auth.getName()));
    }

    @Override
    public UUID requireCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(UnauthorizedException::new);
    }

    @Override
    public Optional<String> getCurrentUsername() {
        return getAuthentication().map(Authentication::getName);
    }

    @Override
    public Set<String> getCurrentRoles() {
        return getAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    private Optional<Authentication> getAuthentication() {
        var ctx = SecurityContextHolder.getContext();
        var auth = ctx.getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return Optional.of(auth);
    }

    private UUID parseUserId(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid user ID format in token: " + subject);
        }
    }
}
