package com.ecommerce.sharedkernel.infrastructure.security;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SecurityContext {

    Optional<UUID> getCurrentUserId();

    UUID requireCurrentUserId();

    Optional<String> getCurrentUsername();

    Set<String> getCurrentRoles();

    default boolean hasRole(String role) {
        return getCurrentRoles().contains(role);
    }

    default boolean isAuthenticated() {
        return getCurrentUserId().isPresent();
    }
}
