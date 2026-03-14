package com.ecommerce.identity.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class IdentityUser {

    private final String userId;
    private final String email;
    private String displayName;
    private final LocalDateTime createdAt;

    public IdentityUser(String userId, String email, String displayName, LocalDateTime createdAt) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.displayName = Objects.requireNonNull(displayName, "displayName must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void changeDisplayName(String newDisplayName) {
        if (newDisplayName == null || newDisplayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        this.displayName = newDisplayName.trim();
    }
}