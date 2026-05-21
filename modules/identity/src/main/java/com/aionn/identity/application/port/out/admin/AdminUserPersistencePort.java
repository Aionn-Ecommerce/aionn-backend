package com.aionn.identity.application.port.out.admin;

import com.aionn.identity.domain.model.IdentityUser;
import com.aionn.identity.domain.valueobject.UserRole;
import com.aionn.identity.domain.valueobject.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Port interface for admin user persistence operations.
 * Provides database-level filtering and pagination for efficient user queries.
 */
public interface AdminUserPersistencePort {
    /**
     * Finds a user by ID.
     *
     * @param userId the user ID
     * @return Optional containing the user if found
     */
    Optional<IdentityUser> findById(String userId);

    /**
     * Saves a user entity.
     *
     * @param user the user to save
     * @return the saved user
     */
    IdentityUser save(IdentityUser user);

    /**
     * Finds users with optional filtering by status and role, with database-level
     * pagination.
     * Uses JOIN FETCH to avoid N+1 queries for user relationships.
     *
     * @param status   the user status filter (null for no filter)
     * @param role     the user role filter (null for no filter)
     * @param pageable pagination parameters
     * @return page of users matching the criteria
     */
    Page<IdentityUser> findUsersWithFilters(UserStatus status, UserRole role, Pageable pageable);
}

