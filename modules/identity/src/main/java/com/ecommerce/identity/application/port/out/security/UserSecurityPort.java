package com.ecommerce.identity.application.port.out.security;

import java.util.Optional;

/**
 * Port interface for user security operations.
 * Provides methods for retrieving user security information.
 */
public interface UserSecurityPort {

    /**
     * Finds a user by their ID.
     *
     * @param userId the user ID
     * @return optional containing user security data if found
     */
    Optional<UserSecurityData> findById(String userId);

    /**
     * Finds a user by email, phone, or username.
     *
     * @param identity the user's email, phone, or username
     * @return optional containing user security data if found
     */
    Optional<UserSecurityData> findByIdentity(String identity);

    /**
     * Data class for user security information.
     */
    record UserSecurityData(String userId, String passwordHash) {
    }
}
