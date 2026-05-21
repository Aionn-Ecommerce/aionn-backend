package com.aionn.identity.application.port.out.security;

import com.aionn.identity.infrastructure.persistence.entity.SecurityAuditEntity;

import java.util.List;

/**
 * Port interface for security audit logging operations.
 * Provides methods for recording and retrieving security audit logs.
 */
public interface SecurityAuditPort {

    /**
     * Saves a security audit log entry.
     *
     * @param userId      the user ID
     * @param eventType   the type of security event
     * @param description the event description
     * @param ipAddress   the IP address from which the event originated
     */
    void saveAuditLog(String userId, String eventType, String description, String ipAddress);

    /**
     * Retrieves audit logs for a user.
     *
     * @param userId the user ID
     * @return list of audit log entities
     */
    List<SecurityAuditEntity> getAuditLogs(String userId);
}

