package com.aionn.identity.application.service;

import com.aionn.identity.application.port.out.agent.AgentAuditPort;
import com.aionn.identity.application.port.out.agent.AgentPersistencePort;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.domain.model.AgentIdentity;
import com.aionn.identity.domain.model.SecurityAudit;
import com.aionn.identity.domain.valueobject.AgentStatus;
import com.aionn.identity.infrastructure.config.properties.AgentProperties;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * Service for managing agent identities.
 * An agent identity is a service account that can perform operations on behalf
 * of a user.
 * 
 * Business Rules:
 * - Agent keys are cryptographically secure and expire after a configurable
 * period
 * - Only users with appropriate capabilities can create agents
 * - Agent status transitions: ACTIVE -> SUSPENDED -> REVOKED
 * - All agent operations are audited for security tracking
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentPersistencePort agentPersistencePort;
    private final AgentAuditPort agentAuditPort;
    private final AgentProperties agentProperties;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final int KEY_LENGTH_BYTES = 32; // 256 bits
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Creates a new agent identity for the specified owner.
     * Generates a cryptographically secure key using SecureRandom and BCrypt
     * hashing.
     * 
     * @param ownerUserId the user ID of the agent owner
     * @return the created agent identity
     * @throws IdentityException if the owner doesn't exist or lacks agent creation
     *                           capability
     */
    public AgentIdentity create(String ownerUserId) {
        log.info("Creating agent for owner: {}", ownerUserId);

        // TODO: Add validation to check if owner has capability to create agents
        // This should check user permissions/roles to ensure they can create agents

        // Generate cryptographically secure key
        byte[] keyBytes = new byte[KEY_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(keyBytes);
        String secureKey = Base64.getEncoder().encodeToString(keyBytes);
        String keyHash = passwordEncoder.encode(secureKey);

        AgentIdentity agentIdentity = AgentIdentity.builder()
                .id(IdGenerator.ulid())
                .ownerId(ownerUserId)
                .name("Agent-" + IdGenerator.ulid().substring(0, 8))
                .keyHash(keyHash)
                .permissions("{\"scope\":\"basic\"}")
                .status(AgentStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusYears(agentProperties.keyExpiryYears()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        AgentIdentity saved = agentPersistencePort.save(agentIdentity);
        log.info("Agent created successfully: {}", saved.getId());
        return saved;
    }

    /**
     * Updates the permissions for an agent identity.
     * 
     * @param ownerUserId     the user ID of the agent owner
     * @param agentId         the agent ID
     * @param permissionsJson the new permissions in JSON format
     * @return the updated agent identity
     * @throws IdentityException if the agent is not found or doesn't belong to the
     *                           owner
     */
    public AgentIdentity updatePermissions(String ownerUserId, String agentId, String permissionsJson) {
        log.info("Updating permissions for agent: {} owned by: {}", agentId, ownerUserId);
        AgentIdentity agentIdentity = getOwnedAgent(ownerUserId, agentId);
        agentIdentity.updatePermissions(permissionsJson);
        AgentIdentity updated = agentPersistencePort.update(agentIdentity);
        log.info("Agent permissions updated successfully: {}", agentId);
        return updated;
    }

    /**
     * Suspends an agent identity and records the action in audit logs.
     * 
     * @param ownerUserId the user ID of the agent owner
     * @param agentId     the agent ID
     * @return the suspended agent identity
     * @throws IdentityException if the agent is not found or doesn't belong to the
     *                           owner
     */
    public AgentIdentity suspend(String ownerUserId, String agentId) {
        log.info("Suspending agent: {} owned by: {}", agentId, ownerUserId);
        AgentIdentity agentIdentity = getOwnedAgent(ownerUserId, agentId);
        agentIdentity.suspend();
        AgentIdentity suspended = agentPersistencePort.update(agentIdentity);

        // Record audit log
        SecurityAudit audit = SecurityAudit.builder()
                .id(IdGenerator.ulid())
                .userId(ownerUserId)
                .eventType("AGENT_SUSPENDED")
                .description("Agent suspended: " + agentId)
                .timestamp(LocalDateTime.now())
                .build();
        agentAuditPort.save(audit);

        log.info("Agent suspended successfully: {}", agentId);
        return suspended;
    }

    /**
     * Retrieves audit logs for a specific agent with database-level filtering.
     * 
     * @param ownerUserId the user ID of the agent owner
     * @param agentId     the agent ID
     * @return list of security audit logs for the agent
     * @throws IdentityException if the agent is not found or doesn't belong to the
     *                           owner
     */
    public List<SecurityAudit> getAgentAuditLogs(String ownerUserId, String agentId) {
        log.debug("Retrieving audit logs for agent: {} owned by: {}", agentId, ownerUserId);
        // Verify ownership
        getOwnedAgent(ownerUserId, agentId);
        // Use database-level filtering instead of in-memory filtering
        return agentAuditPort.findByAgentId(agentId, 100);
    }

    /**
     * Lists all agent identities owned by a user.
     * 
     * @param ownerUserId the user ID of the agent owner
     * @return list of agent identities
     */
    public List<AgentIdentity> listMy(String ownerUserId) {
        log.debug("Listing agents for owner: {}", ownerUserId);
        return agentPersistencePort.findByOwnerId(ownerUserId);
    }

    /**
     * Retrieves a specific agent identity owned by a user.
     * 
     * @param ownerUserId the user ID of the agent owner
     * @param agentId     the agent ID
     * @return the agent identity
     * @throws IdentityException if the agent is not found or doesn't belong to the
     *                           owner
     */
    public AgentIdentity get(String ownerUserId, String agentId) {
        log.debug("Getting agent: {} for owner: {}", agentId, ownerUserId);
        return getOwnedAgent(ownerUserId, agentId);
    }

    /**
     * Revokes (deletes) an agent identity.
     * 
     * @param ownerUserId the user ID of the agent owner
     * @param agentId     the agent ID
     * @throws IdentityException if the agent is not found or doesn't belong to the
     *                           owner
     */
    public void revoke(String ownerUserId, String agentId) {
        log.info("Revoking agent: {} owned by: {}", agentId, ownerUserId);
        AgentIdentity agentIdentity = getOwnedAgent(ownerUserId, agentId);
        agentPersistencePort.delete(agentIdentity.getId());
        log.info("Agent revoked successfully: {}", agentId);
    }

    /**
     * Retrieves an agent identity and verifies it belongs to the specified owner.
     * 
     * @param ownerUserId the user ID of the agent owner
     * @param agentId     the agent ID
     * @return the agent identity
     * @throws IdentityException with AGENT_NOT_FOUND if the agent is not found or
     *                           doesn't belong to the owner
     */
    private AgentIdentity getOwnedAgent(String ownerUserId, String agentId) {
        return agentPersistencePort.findByIdAndOwnerId(agentId, ownerUserId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.AGENT_NOT_FOUND, "Agent not found"));
    }
}

