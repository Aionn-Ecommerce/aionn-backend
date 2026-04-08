package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.mapper.AdminResultMapper;
import com.ecommerce.identity.application.port.out.admin.AdminUserPersistencePort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.domain.valueobject.UserRole;
import com.ecommerce.identity.domain.valueobject.UserStatus;
import com.ecommerce.identity.application.dto.admin.result.UserDetailResult;
import com.ecommerce.identity.application.dto.admin.result.UserListResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for administrative user management operations.
 * Handles role management, status updates, account unlocking, and user listing.
 * 
 * <p>
 * <strong>Authorization Note:</strong> All methods in this service assume the
 * caller
 * has been authorized at the Controller or UseCase level. Authorization checks
 * should
 * verify the caller has ADMIN role before invoking these operations.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserPersistencePort adminUserPersistencePort;
    private final AdminResultMapper adminResultMapper;

    /**
     * Updates the roles for a user.
     * 
     * <p>
     * <strong>Authorization:</strong> Caller must have ADMIN role (verified at
     * UseCase/Controller level)
     * </p>
     *
     * @param userId the ID of the user to update
     * @param roles  the new set of roles to assign
     * @return the updated set of role names
     * @throws IdentityException if user not found
     */
    public Set<String> updateRoles(String userId, Set<String> roles) {
        log.info("Updating roles for user: {} with roles: {}", userId, roles);
        IdentityUser user = getUser(userId);
        user.setRoles(normalizeRoles(roles).stream()
                .map(UserRole::valueOf)
                .collect(Collectors.toSet()));
        IdentityUser saved = adminUserPersistencePort.save(user);
        log.info("Successfully updated roles for user: {}", userId);
        return saved.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
    }

    /**
     * Removes specific roles from a user.
     * Ensures at least BUYER role remains if all roles would be removed.
     *
     * @param userId the ID of the user
     * @param roles  the roles to remove
     * @return the remaining set of role names
     * @throws IdentityException if user not found
     */
    public Set<String> removeRoles(String userId, Set<String> roles) {
        log.info("Removing roles from user: {} - roles to remove: {}", userId, roles);
        IdentityUser user = getUser(userId);
        Set<String> normalized = normalizeRoles(roles);
        Set<UserRole> remaining = user.getRoles().stream()
                .filter(existing -> !normalized.contains(existing.name()))
                .collect(Collectors.toSet());
        if (remaining.isEmpty()) {
            log.debug("All roles removed, assigning default BUYER role to user: {}", userId);
            remaining.add(UserRole.BUYER);
        }
        user.setRoles(remaining);
        IdentityUser saved = adminUserPersistencePort.save(user);
        log.info("Successfully removed roles from user: {}", userId);
        return saved.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
    }

    /**
     * Updates the status of a user.
     *
     * @param userId the ID of the user
     * @param status the new status
     * @return the updated status name
     * @throws IdentityException if user not found
     */
    public String updateStatus(String userId, String status) {
        log.info("Updating status for user: {} to status: {}", userId, status);
        IdentityUser user = getUser(userId);
        user.updateStatus(UserStatus.valueOf(status.toUpperCase()));
        IdentityUser saved = adminUserPersistencePort.save(user);
        log.info("Successfully updated status for user: {} to {}", userId, saved.getStatus());
        return saved.getStatus().name();
    }

    /**
     * Unlocks a user account by clearing the locked_until timestamp.
     *
     * @param userId the ID of the user to unlock
     * @throws IdentityException if user not found
     */
    public void unlockAccount(String userId) {
        log.info("Unlocking account for user: {}", userId);
        IdentityUser user = getUser(userId);
        user.setLockedUntil(null);
        adminUserPersistencePort.save(user);
        log.info("Successfully unlocked account for user: {}", userId);
    }

    /**
     * Lists users with optional filtering by status and role.
     * Uses database-level pagination and JOIN FETCH to avoid N+1 queries.
     *
     * @param status the status filter (null or empty for no filter)
     * @param role   the role filter (null or empty for no filter)
     * @param page   the page number (0-indexed)
     * @param size   the page size
     * @return paginated list of users matching the criteria
     */
    public UserListResult listUsers(String status, String role, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        UserStatus statusFilter = parseStatus(status);
        UserRole roleFilter = parseRole(role);

        log.debug("Listing users with filters - status: {}, role: {}, page: {}, size: {}",
                statusFilter, roleFilter, safePage, safeSize);

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<IdentityUser> userPage = adminUserPersistencePort.findUsersWithFilters(
                statusFilter, roleFilter, pageable);

        var users = userPage.getContent().stream()
                .map(user -> new UserListResult.UserSummary(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getStatus() != null ? user.getStatus().name() : null,
                        user.getRoles().stream().findFirst().map(Enum::name).orElse(null)))
                .toList();

        log.debug("Retrieved {} users out of {} total", users.size(), userPage.getTotalElements());
        return adminResultMapper.toUserListResult(users, safePage, safeSize, (int) userPage.getTotalElements());
    }

    /**
     * Retrieves detailed information about a specific user.
     *
     * @param userId the ID of the user
     * @return detailed user information
     * @throws IdentityException if user not found
     */
    public UserDetailResult getUserById(String userId) {
        log.debug("Retrieving user details for user: {}", userId);
        IdentityUser user = getUser(userId);
        return adminResultMapper.toUserDetailResult(user);
    }

    private IdentityUser getUser(String userId) {
        return adminUserPersistencePort.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new IdentityException(IdentityErrorCode.USER_NOT_FOUND);
                });
    }

    private Set<String> normalizeRoles(Set<String> roles) {
        return roles.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }

    private UserStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return UserStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status filter: {}", status);
            return null;
        }
    }

    private UserRole parseRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return null;
        }
        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role filter: {}", role);
            return null;
        }
    }
}
