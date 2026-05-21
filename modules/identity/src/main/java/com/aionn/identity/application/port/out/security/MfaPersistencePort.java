package com.aionn.identity.application.port.out.security;

import java.util.List;

/**
 * Port interface for MFA (Multi-Factor Authentication) persistence operations.
 * Provides methods for managing MFA settings and backup codes.
 */
public interface MfaPersistencePort {

    /**
     * Updates the MFA enabled status for a user.
     *
     * @param userId  the user ID
     * @param enabled true to enable MFA, false to disable
     */
    void updateMfaStatus(String userId, boolean enabled);

    /**
     * Deletes all backup codes for a user.
     *
     * @param userId the user ID
     */
    void deleteBackupCodes(String userId);

    /**
     * Saves backup codes for a user.
     *
     * @param userId     the user ID
     * @param codeHashes list of hashed backup codes
     * @return list of backup code IDs
     */
    List<String> saveBackupCodes(String userId, List<String> codeHashes);
}

