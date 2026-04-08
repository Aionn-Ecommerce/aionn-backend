package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.port.out.security.PasswordHasher;
import com.ecommerce.identity.application.port.out.security.PasswordResetPort;
import com.ecommerce.identity.application.port.out.security.SecurityAuditPort;
import com.ecommerce.identity.application.port.out.security.UserSecurityPort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for managing password reset and change operations.
 * Handles password changes, reset requests, and reset completion.
 * 
 * <p>
 * Security Rules:
 * <ul>
 * <li>Password changes require current password verification</li>
 * <li>Reset tokens expire after 15 minutes</li>
 * <li>Reset tokens are single-use and deleted after use</li>
 * <li>All password operations are audited</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserSecurityPort userSecurityPort;
    private final PasswordResetPort passwordResetPort;
    private final SecurityAuditPort securityAuditPort;
    private final PasswordHasher passwordHasher;

    /**
     * Changes a user's password.
     * Requires the current password for verification.
     *
     * @param userId          the user ID
     * @param currentPassword the user's current password
     * @param newPassword     the new password
     * @param ipAddress       the IP address from which the request originated
     * @throws IdentityException if user not found or current password is invalid
     */
    public void changePassword(String userId, String currentPassword, String newPassword, String ipAddress) {
        log.info("Processing password change for user: {}", userId);
        var user = getUserSecurityData(userId);

        if (!passwordHasher.matches(currentPassword, user.passwordHash())) {
            log.warn("Failed password change attempt for user {} - invalid current password", userId);
            throw new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS);
        }

        String newPasswordHash = passwordHasher.hash(newPassword);
        passwordResetPort.updatePassword(userId, newPasswordHash);
        securityAuditPort.saveAuditLog(userId, "PASSWORD_CHANGED", "User changed password", ipAddress);
        log.info("Password changed successfully for user: {}", userId);
    }

    /**
     * Requests a password reset for a user.
     * Generates a reset token that expires in 15 minutes.
     *
     * @param identity  the user's email, phone, or username
     * @param ipAddress the IP address from which the request originated
     * @return the password reset token
     * @throws IdentityException if user not found
     */
    public String requestPasswordReset(String identity, String ipAddress) {
        log.info("Processing password reset request for identity: {}", identity);
        var user = findByIdentity(identity);

        String token = IdGenerator.ulid();
        passwordResetPort.savePasswordResetToken(token, user.userId(), LocalDateTime.now().plusMinutes(15));
        securityAuditPort.saveAuditLog(user.userId(), "PASSWORD_RESET_REQUESTED", "User requested password reset",
                ipAddress);
        log.info("Password reset requested for user: {}, token: {}", user.userId(), token);
        return token;
    }

    /**
     * Completes a password reset using a reset token.
     * The token is validated and deleted after use.
     *
     * @param token       the password reset token
     * @param newPassword the new password
     * @param ipAddress   the IP address from which the request originated
     * @throws IdentityException if token is invalid or expired
     */
    public void completePasswordReset(String token, String newPassword, String ipAddress) {
        log.info("Processing password reset completion with token: {}", token);
        var data = passwordResetPort.findPasswordResetToken(token)
                .orElseThrow(() -> {
                    log.warn("Invalid password reset token: {}", token);
                    return new IdentityException(IdentityErrorCode.VERIFICATION_TOKEN_INVALID);
                });

        if (data.expiresAt().isBefore(LocalDateTime.now())) {
            passwordResetPort.deletePasswordResetToken(token);
            log.warn("Expired password reset token: {}", token);
            throw new IdentityException(IdentityErrorCode.OTP_EXPIRED);
        }

        String newPasswordHash = passwordHasher.hash(newPassword);
        passwordResetPort.updatePassword(data.userId(), newPasswordHash);
        passwordResetPort.deletePasswordResetToken(token);
        securityAuditPort.saveAuditLog(data.userId(), "PASSWORD_RESET_COMPLETED", "Password reset completed with token",
                ipAddress);
        log.info("Password reset completed successfully for user: {}", data.userId());
    }

    private UserSecurityPort.UserSecurityData getUserSecurityData(String userId) {
        return userSecurityPort.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
    }

    private UserSecurityPort.UserSecurityData findByIdentity(String identity) {
        return userSecurityPort.findByIdentity(identity)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
    }
}
