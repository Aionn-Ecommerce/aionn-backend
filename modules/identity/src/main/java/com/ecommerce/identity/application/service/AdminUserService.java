package com.ecommerce.identity.application.service;

import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.valueobject.UserRole;
import com.ecommerce.identity.domain.valueobject.UserStatus;
import com.ecommerce.identity.application.dto.admin.UserDetailResult;
import com.ecommerce.identity.application.dto.admin.UserListResult;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional
    public Set<String> updateRoles(String userId, Set<String> roles) {
        UserEntity user = getUser(userId);
        user.setRoles(normalizeRoles(roles).stream()
                .map(UserRole::valueOf)
                .collect(Collectors.toSet()));
        return userRepository.save(user).getRoles().stream().map(Enum::name).collect(Collectors.toSet());
    }

    @Transactional
    public Set<String> removeRoles(String userId, Set<String> roles) {
        UserEntity user = getUser(userId);
        Set<String> normalized = normalizeRoles(roles);
        Set<UserRole> remaining = user.getRoles().stream()
                .filter(existing -> !normalized.contains(existing.name()))
                .collect(Collectors.toSet());
        if (remaining.isEmpty()) {
            remaining.add(UserRole.BUYER);
        }
        user.setRoles(remaining);
        return userRepository.save(user).getRoles().stream().map(Enum::name).collect(Collectors.toSet());
    }

    @Transactional
    public String updateStatus(String userId, String status) {
        UserEntity user = getUser(userId);
        user.setStatus(UserStatus.valueOf(status.toUpperCase()));
        return userRepository.save(user).getStatus().name();
    }

    @Transactional
    public void unlockAccount(String userId) {
        UserEntity user = getUser(userId);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserListResult listUsers(String status, String role, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        String normalizedStatus = normalizeText(status);
        String normalizedRole = normalizeText(role);

        List<UserEntity> filtered = userRepository.findAll().stream()
                .filter(user -> matchesStatus(user, normalizedStatus))
                .filter(user -> matchesRole(user, normalizedRole))
                .sorted(Comparator.comparing(UserEntity::getCreatedAt).reversed())
                .toList();

        int fromIndex = Math.min(safePage * safeSize, filtered.size());
        int toIndex = Math.min(fromIndex + safeSize, filtered.size());

        List<UserListResult.UserSummary> users = filtered.subList(fromIndex, toIndex).stream()
                .map(user -> new UserListResult.UserSummary(
                        user.getUserId(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getStatus() != null ? user.getStatus().name() : null,
                        user.getRoles().stream().findFirst().map(Enum::name).orElse(null)))
                .toList();

        return new UserListResult(users, safePage, safeSize, filtered.size());
    }

    @Transactional(readOnly = true)
    public UserDetailResult getUserById(String userId) {
        UserEntity user = getUser(userId);
        return new UserDetailResult(
                user.getUserId(),
                user.getEmail(),
                user.getPhone(),
                user.getDisplayName(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                user.getStatus() != null ? user.getStatus().name() : null,
                user.getCreatedAt(),
                user.getEmailVerifiedAt(),
                user.getPhoneVerifiedAt());
    }

    private UserEntity getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
    }

    private Set<String> normalizeRoles(Set<String> roles) {
        return roles.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean matchesStatus(UserEntity user, String status) {
        if (status == null) {
            return true;
        }
        return user.getStatus() != null && status.equals(user.getStatus().name());
    }

    private boolean matchesRole(UserEntity user, String role) {
        if (role == null) {
            return true;
        }
        return user.getRoles().stream().anyMatch(userRole -> role.equals(userRole.name()));
    }
}
