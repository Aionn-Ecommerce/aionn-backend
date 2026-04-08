package com.ecommerce.identity.application.port.out.agent;

import com.ecommerce.identity.domain.model.AgentIdentity;

import java.util.List;
import java.util.Optional;

/**
 * Port interface for agent identity persistence operations.
 * Provides methods to manage agent identities in the persistence layer.
 */
public interface AgentPersistencePort {

    /**
     * Saves a new agent identity.
     *
     * @param agentIdentity the agent identity to save
     * @return the saved agent identity
     */
    AgentIdentity save(AgentIdentity agentIdentity);

    /**
     * Finds an agent identity by its ID.
     *
     * @param agentId the agent ID
     * @return an Optional containing the agent identity if found
     */
    Optional<AgentIdentity> findById(String agentId);

    /**
     * Finds all agent identities owned by a specific user.
     *
     * @param ownerId the owner user ID
     * @return list of agent identities owned by the user
     */
    List<AgentIdentity> findByOwnerId(String ownerId);

    /**
     * Finds an agent identity by its key hash.
     *
     * @param keyHash the key hash
     * @return an Optional containing the agent identity if found
     */
    Optional<AgentIdentity> findByKeyHash(String keyHash);

    /**
     * Finds an agent identity by ID and owner ID.
     *
     * @param agentId the agent ID
     * @param ownerId the owner user ID
     * @return an Optional containing the agent identity if found
     */
    Optional<AgentIdentity> findByIdAndOwnerId(String agentId, String ownerId);

    /**
     * Updates an existing agent identity.
     *
     * @param agentIdentity the agent identity to update
     * @return the updated agent identity
     */
    AgentIdentity update(AgentIdentity agentIdentity);

    /**
     * Deletes an agent identity by its ID.
     *
     * @param agentId the agent ID to delete
     */
    void delete(String agentId);
}
