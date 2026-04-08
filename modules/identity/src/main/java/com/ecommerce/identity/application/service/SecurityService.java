package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.port.out.security.PasswordHasher;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.infrastructure.persistence.entity.BackupCodeEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.SecurityAuditEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import com.ecommerce.identity.infrastructure.persistence.repository.security.BackupCodeRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.security.SecurityAuditRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import com.ecommerce.identity.infrastructure.security.InMemoryPasswordResetTokenStore;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @deprecated This service has been split into separate services for better
 *             separation of concerns:
 *             - {@link MfaService} for MFA operations (enableMfa, disableMfa,
 *             regenerateBackupCodes)
 *             - {@link PasswordResetService} for password reset operations
 *             (changePassword, requestPasswordReset, completePasswordReset)
 *             - {@link SecurityAuditService} for audit log retrieval
 *             (getAuditLogs)
 * 
 *             This class is kept temporarily for backward compatibility with
 *             existing tests.
 *             New code should use the specific services through their
 *             respective UseCases.
 */
@Deprecated(since = "2.0", forRemoval = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    private final UserRepository userRepository;
    private final BackupCodeRepository backupCodeRepository;
    private final SecurityAuditRepository auditRepository;
    private final PasswordHasher passwordHasher;
    private final InMemoryPasswordResetTokenStore passwordResetTokenStore;

    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword, String ipAddress) {
        UserEntity user = getUser(userId);
        if (!passwordHasher.matches(currentPassword, user.getPasswordHash())) {
            throw new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS);
        }
        user.setPasswordHash(passwordHasher.hash(newPassword));
        userRepository.save(user);
        saveAudit(user, "PASSWORD_CHANGED", "User changed password", ipAddress);
    }

    @Transactional
    public String requestPasswordReset(String identity, String ipAddress) {
        UserEntity user = findByIdentity(identity);
        String token = IdGenerator.ulid();
        passwordResetTokenStore.save(token, user.getUserId(), LocalDateTime.now().plusMinutes(15));
        saveAudit(user, "PASSWORD_RESET_REQUESTED", "User requested password reset", ipAddress);
        log.info("[PASSWORD-RESET] userId={}, token={}", user.getUserId(), token);
        return token;
    }

    @Transactional
    public void completePasswordReset(String token, String newPassword, String ipAddress) {
        var data = passwordResetTokenStore.find(token)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.VERIFICATION_TOKEN_INVALID));
        if (data.expiresAt().isBefore(LocalDateTime.now())) {
            passwordResetTokenStore.delete(token);
            throw new IdentityException(IdentityErrorCode.OTP_EXPIRED);
        }
        UserEntity user = getUser(data.userId());
        user.setPasswordHash(passwordHasher.hash(newPassword));
        userRepository.save(user);
        passwordResetTokenStore.delete(token);
        saveAudit(user, "PASSWORD_RESET_COMPLETED", "Password reset completed with token", ipAddress);
    }

    @Transactional
    public boolean enableMfa(String userId, String password, String ipAddress) {
        UserEntity user = getUser(userId);
        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS);
        }
        user.setMfaEnabled(true);
        userRepository.save(user);
        saveAudit(user, "MFA_ENABLED", "MFA enabled", ipAddress);
        return true;
    }

    @Transactional
    public boolean disableMfa(String userId, String password, String ipAddress) {
        UserEntity user = getUser(userId);
        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS);
        }
        user.setMfaEnabled(false);
        userRepository.save(user);
        saveAudit(user, "MFA_DISABLED", "MFA disabled", ipAddress);
        return false;
    }

    @Transactional
    public List<String> regenerateBackupCodes(String userId, String ipAddress) {
        UserEntity user = getUser(userId);
        backupCodeRepository.deleteByUser_UserId(userId);
        List<String> rawCodes = java.util.stream.IntStream.range(0, 8)
                .mapToObj(i -> String.format("%08d", ThreadLocalRandom.current().nextInt(0, 100_000_000)))
                .toList();
        List<BackupCodeEntity> entities = rawCodes.stream()
                .map(code -> BackupCodeEntity.builder()
                        .backupCodeId(IdGenerator.ulid())
                        .user(user)
                        .codeHash(passwordHasher.hash(code))
                        .build())
                .toList();
        backupCodeRepository.saveAll(entities);
        saveAudit(user, "MFA_BACKUP_CODES_REGENERATED", "Backup codes regenerated", ipAddress);
        return rawCodes;
    }

    @Transactional(readOnly = true)
    public List<SecurityAuditEntity> getAuditLogs(String userId) {
        return auditRepository.findTop100ByUser_UserIdOrderByTimestampDesc(userId);
    }

    private UserEntity getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
    }

    private UserEntity findByIdentity(String identity) {
        return userRepository.findByEmailIgnoreCase(identity)
                .or(() -> userRepository.findByPhone(identity))
                .or(() -> userRepository.findByUsernameIgnoreCase(identity))
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
    }

    private void saveAudit(UserEntity user, String eventType, String description, String ipAddress) {
        auditRepository.save(SecurityAuditEntity.builder()
                .auditId(IdGenerator.ulid())
                .user(user)
                .eventType(eventType)
                .description(description)
                .ipAddress(ipAddress)
                .deviceId(null)
                .build());
    }
}
