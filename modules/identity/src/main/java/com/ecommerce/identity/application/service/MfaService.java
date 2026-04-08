package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.port.out.security.MfaPersistencePort;
import com.ecommerce.identity.application.port.out.security.PasswordHasher;
import com.ecommerce.identity.application.port.out.security.SecurityAuditPort;
import com.ecommerce.identity.application.port.out.security.UserSecurityPort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service for managing Multi-Factor Authentication (MFA) operations.
 * Handles enabling/disabling MFA and managing backup codes.
 * 
 * <p>
 * Security Rules:
 * <ul>
 * <li>MFA operations require password verification</li>
 * <li>Backup codes are generated as 8-digit numbers</li>
 * <li>Backup codes are hashed before storage</li>
 * <li>All MFA operations are audited</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MfaService {

    private final UserSecurityPort userSecurityPort;
    private final MfaPersistencePort mfaPersistencePort;
    private final SecurityAuditPort securityAuditPort;
    private final PasswordHasher passwordHasher;

    /**
     * Enables MFA for a user.
     *
     * @param userId    the user ID
     * @param password  the user's current password for verification
     * @param ipAddress the IP address from which the request originated
     * @return true if MFA was enabled successfully
     * @throws IdentityException if user not found or password is invalid
     */
    public boolean enableMfa(String userId, String password, String ipAddress) {
        log.info("Enabling MFA for user: {}", userId);
        var user = getUserSecurityData(userId);

        if (!passwordHasher.matches(password, user.passwordHash())) {
            log.warn("Failed MFA enable attempt for user {} - invalid password", userId);
            throw new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS);
        }

        mfaPersistencePort.updateMfaStatus(userId, true);
        securityAuditPort.saveAuditLog(userId, "MFA_ENABLED", "MFA enabled", ipAddress);
        log.info("MFA enabled successfully for user: {}", userId);
        return true;
    }

    /**
     * Disables MFA for a user.
     *
     * @param userId    the user ID
     * @param password  the user's current password for verification
     * @param ipAddress the IP address from which the request originated
     * @return false to indicate MFA is now disabled
     * @throws IdentityException if user not found or password is invalid
     */
    public boolean disableMfa(String userId, String password, String ipAddress) {
        log.info("Disabling MFA for user: {}", userId);
        var user = getUserSecurityData(userId);

        if (!passwordHasher.matches(password, user.passwordHash())) {
            log.warn("Failed MFA disable attempt for user {} - invalid password", userId);
            throw new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS);
        }

        mfaPersistencePort.updateMfaStatus(userId, false);
        securityAuditPort.saveAuditLog(userId, "MFA_DISABLED", "MFA disabled", ipAddress);
        log.info("MFA disabled successfully for user: {}", userId);
        return false;
    }

    /**
     * Regenerates backup codes for a user.
     * Deletes all existing backup codes and generates 8 new ones.
     *
     * @param userId    the user ID
     * @param ipAddress the IP address from which the request originated
     * @return list of 8 raw backup codes (unhashed) to display to the user
     * @throws IdentityException if user not found
     */
    public List<String> regenerateBackupCodes(String userId, String ipAddress) {
        log.info("Regenerating backup codes for user: {}", userId);
        getUserSecurityData(userId); // Verify user exists

        mfaPersistencePort.deleteBackupCodes(userId);

        List<String> rawCodes = java.util.stream.IntStream.range(0, 8)
                .mapToObj(i -> String.format("%08d", ThreadLocalRandom.current().nextInt(0, 100_000_000)))
                .toList();

        List<String> codeHashes = rawCodes.stream()
                .map(passwordHasher::hash)
                .toList();

        mfaPersistencePort.saveBackupCodes(userId, codeHashes);
        securityAuditPort.saveAuditLog(userId, "MFA_BACKUP_CODES_REGENERATED", "Backup codes regenerated", ipAddress);
        log.info("Backup codes regenerated successfully for user: {}", userId);
        return rawCodes;
    }

    private UserSecurityPort.UserSecurityData getUserSecurityData(String userId) {
        return userSecurityPort.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
    }
}
