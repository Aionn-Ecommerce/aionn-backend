package com.ecommerce.identity.domain.model;

import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.id.UserId;
import com.ecommerce.identity.domain.valueobject.UserRole;
import com.ecommerce.identity.domain.valueobject.UserStatus;
import com.ecommerce.sharedkernel.domain.model.Entity;

import java.util.LinkedHashSet;
import java.util.Set;
import java.time.LocalDateTime;

public class IdentityUser extends Entity<UserId> {

    private String email;
    private String phone;
    private final String username;
    private String passwordHash;
    private String displayName;
    private String avatarUrl;
    private final Set<UserRole> roles;
    private UserStatus status;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime phoneVerifiedAt;
    private final LocalDateTime createdAt;

    public IdentityUser(
            UserId id,
            String email,
            String phone,
            String username,
            String passwordHash,
            String displayName,
            String avatarUrl,
            Set<UserRole> roles,
            UserStatus status,
            LocalDateTime emailVerifiedAt,
            LocalDateTime phoneVerifiedAt,
            LocalDateTime createdAt) {
        super(id);
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.roles = (roles == null || roles.isEmpty())
                ? new LinkedHashSet<>(Set.of(UserRole.BUYER))
                : new LinkedHashSet<>(roles);
        this.status = status;
        this.emailVerifiedAt = emailVerifiedAt;
        this.phoneVerifiedAt = phoneVerifiedAt;
        this.createdAt = createdAt;
    }

    public static IdentityUser createNew(UserId userId, String email, String phone, String username) {
        return new IdentityUser(
                userId,
                email,
                phone,
                username,
                null,
                null, // displayName mặc định null hoặc set theo email
                null, // avatarUrl
                Set.of(UserRole.BUYER),
                UserStatus.ACTIVE,
                null,
                null,
                LocalDateTime.now());
    }

    public void updateProfile(String displayName, String avatarUrl) {
        if (displayName != null && displayName.isBlank()) {
            throw new IdentityException(IdentityErrorCode.INVALID_DISPLAY_NAME);
        }
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    public void updateDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IdentityException(IdentityErrorCode.INVALID_DISPLAY_NAME);
        }
        this.displayName = displayName.trim();
    }

    public void updateAvatar(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void verifyEmail() {
        if (this.emailVerifiedAt != null)
            return;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    public void verifyPhone() {
        if (this.phoneVerifiedAt != null)
            return;
        this.phoneVerifiedAt = LocalDateTime.now();
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    public void updatePhone(String newPhone) {
        this.phone = newPhone;
        this.phoneVerifiedAt = LocalDateTime.now();
    }

    public void ban() {
        this.status = UserStatus.BANNED;
    }

    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    public String getUserId() {
        return getId().getValue().toString();
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Set<UserRole> getRoles() {
        return Set.copyOf(roles);
    }

    public void addRole(UserRole role) {
        if (role != null) {
            this.roles.add(role);
        }
    }

    public void removeRole(UserRole role) {
        if (role == null) {
            return;
        }
        this.roles.remove(role);
        if (this.roles.isEmpty()) {
            this.roles.add(UserRole.BUYER);
        }
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public LocalDateTime getPhoneVerifiedAt() {
        return phoneVerifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
