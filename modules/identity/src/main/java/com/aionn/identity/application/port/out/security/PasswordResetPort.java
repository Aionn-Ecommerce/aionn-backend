package com.aionn.identity.application.port.out.security;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Port interface for password reset operations.
 * Provides methods for managing password reset tokens and updating passwords.
 */
public interface PasswordResetPort {

    /**
     * Saves a password reset token.
     *
     * @param token     the reset token
     * @param userId    the user ID
     * @param expiresAt the expiration timestamp
     */
    void savePasswordResetToken(String token, String userId, LocalDateTime expiresAt);

    /**
     * Finds a password reset token.
     *
     * @param token the reset token
     * @return optional containing token data if found
     */
    Optional<PasswordResetTokenData> findPasswordResetToken(String token);

    /**
     * Deletes a password reset token.
     *
     * @param token the reset token
     */
    void deletePasswordResetToken(String token);

    /**
     * Updates a user's password.
     *
     * @param userId       the user ID
     * @param passwordHash the new password hash
     */
    void updatePassword(String userId, String passwordHash);

    /**
     * Data class for password reset token information.
     */
    record PasswordResetTokenData(String userId, LocalDateTime expiresAt) {
    }
}

