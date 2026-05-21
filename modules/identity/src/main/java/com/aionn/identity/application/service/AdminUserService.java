package com.aionn.identity.application.service;

import com.aionn.identity.application.dto.admin.result.UserDetailResult;
import com.aionn.identity.application.dto.admin.result.UserListResult;
import com.aionn.identity.application.mapper.AdminResultMapper;
import com.aionn.identity.application.port.out.admin.AdminUserPersistencePort;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.domain.model.IdentityUser;
import com.aionn.identity.domain.valueobject.UserRole;
import com.aionn.identity.domain.valueobject.UserStatus;
import com.aionn.sharedkernel.domain.vo.OffsetPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Administrative user management. Authorization is enforced at the
 * controller/use-case layer via Spring Security; these methods assume the
 * caller has already passed the role check.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserPersistencePort adminUserPersistencePort;
    private final AdminResultMapper adminResultMapper;

    public Set<String> updateRoles(String userId, Set<String> roles) {
        log.info("Updating roles for user: {}", userId);
        IdentityUser user = getUser(userId);
        user.setRoles(parseRoles(roles));
        IdentityUser saved = adminUserPersistencePort.save(user);
        return saved.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
    }

    public Set<String> removeRoles(String userId, Set<String> roles) {
        log.info("Removing roles from user: {}", userId);
        IdentityUser user = getUser(userId);
        Set<UserRole> toRemove = parseRoles(roles);
        Set<UserRole> remaining = user.getRoles().stream()
                .filter(existing -> !toRemove.contains(existing))
                .collect(Collectors.toSet());
        if (remaining.isEmpty()) {
            remaining.add(UserRole.BUYER);
        }
        user.setRoles(remaining);
        IdentityUser saved = adminUserPersistencePort.save(user);
        return saved.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
    }

    public String updateStatus(String userId, String status) {
        log.info("Updating status for user: {} to {}", userId, status);
        UserStatus parsed = parseStatus(status);
        if (parsed == null) {
            throw new IdentityException(IdentityErrorCode.INVALID_DISPLAY_NAME, "Invalid user status: " + status);
        }
        IdentityUser user = getUser(userId);
        user.updateStatus(parsed);
        IdentityUser saved = adminUserPersistencePort.save(user);
        return saved.getStatus().name();
    }

    /**
     * Clear an account's lock. Persists through the domain entity so the
     * mapper writes {@code locked_until = NULL} on the user row.
     */
    public void unlockAccount(String userId) {
        log.info("Unlocking account for user: {}", userId);
        IdentityUser user = getUser(userId);
        user.unlock();
        adminUserPersistencePort.save(user);
    }

    public UserListResult listUsers(String status, String role, int page, int size) {
        OffsetPagination pagination = OffsetPagination.safe(page, size);

        UserStatus statusFilter = parseStatus(status);
        UserRole roleFilter = parseRole(role);

        log.debug("Listing users status={}, role={}, page={}, size={}", statusFilter, roleFilter,
                pagination.page(), pagination.size());

        Pageable pageable = PageRequest.of(pagination.page(), pagination.size());
        Page<IdentityUser> userPage = adminUserPersistencePort.findUsersWithFilters(
                statusFilter, roleFilter, pageable);

        var users = userPage.getContent().stream()
                .map(user -> new UserListResult.UserSummary(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getStatus() != null ? user.getStatus().name() : null,
                        user.getRoles().stream()
                                .map(Enum::name)
                                .sorted()
                                .collect(Collectors.joining(","))))
                .toList();

        return adminResultMapper.toUserListResult(users, pagination.page(), pagination.size(),
                (int) userPage.getTotalElements());
    }

    public UserDetailResult getUserById(String userId) {
        return adminResultMapper.toUserDetailResult(getUser(userId));
    }

    private IdentityUser getUser(String userId) {
        return adminUserPersistencePort.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
    }

    private static Set<UserRole> parseRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IdentityException(IdentityErrorCode.INSUFFICIENT_PERMISSIONS,
                    "At least one role must be supplied");
        }
        return roles.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> {
                    try {
                        return UserRole.valueOf(s.trim().toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        throw new IdentityException(IdentityErrorCode.INSUFFICIENT_PERMISSIONS,
                                "Unknown role: " + s);
                    }
                })
                .collect(Collectors.toSet());
    }

    private static UserStatus parseStatus(String status) {
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

    private static UserRole parseRole(String role) {
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

