package com.ecommerce.identity.domain.model;

import com.ecommerce.identity.domain.valueobject.AgentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Domain model representing an agent identity.
 * An agent identity is a service account that can perform operations on behalf
 * of a user.
 */
@Getter
@Builder
@AllArgsConstructor
public class AgentIdentity {

    private final String id;
    private final String ownerId;
    private final String name;
    private final String keyHash;
    private String permissions;
    private AgentStatus status;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Updates the permissions for this agent identity.
     *
     * @param permissions the new permissions JSON string
     */
    public void updatePermissions(String permissions) {
        this.permissions = permissions;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Suspends this agent identity.
     */
    public void suspend() {
        this.status = AgentStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Revokes this agent identity.
     */
    public void revoke() {
        this.status = AgentStatus.REVOKED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Activates this agent identity.
     */
    public void activate() {
        this.status = AgentStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if this agent identity is active.
     *
     * @return true if the agent is active, false otherwise
     */
    public boolean isActive() {
        return status == AgentStatus.ACTIVE;
    }

    /**
     * Checks if this agent identity has expired.
     *
     * @return true if the agent has expired, false otherwise
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
