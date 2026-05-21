package com.aionn.identity.application.port.out.agent;

import com.aionn.identity.domain.model.SecurityAudit;

import java.util.List;

/**
 * Port interface for agent audit log operations.
 * Provides methods to manage security audit logs with database-level filtering.
 */
public interface AgentAuditPort {

    /**
     * Saves a new security audit log entry.
     *
     * @param securityAudit the security audit to save
     * @return the saved security audit
     */
    SecurityAudit save(SecurityAudit securityAudit);

    /**
     * Finds audit logs by agent ID with database-level filtering.
     * This method performs filtering at the database level for optimal performance.
     *
     * @param agentId the agent ID to filter by
     * @param limit   the maximum number of results to return
     * @return list of security audit logs for the specified agent
     */
    List<SecurityAudit> findByAgentId(String agentId, int limit);
}

