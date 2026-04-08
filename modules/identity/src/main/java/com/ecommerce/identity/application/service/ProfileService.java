package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.user.command.UpdateAvatarCommand;
import com.ecommerce.identity.application.dto.user.command.UpdateDisplayNameCommand;
import com.ecommerce.identity.application.dto.user.query.GetMyProfileQuery;
import com.ecommerce.identity.application.dto.user.view.UserProfileView;
import com.ecommerce.identity.application.port.in.user.GetMyProfileInputPort;
import com.ecommerce.identity.application.port.in.user.UpdateAvatarInputPort;
import com.ecommerce.identity.application.port.in.user.UpdateDisplayNameInputPort;
import com.ecommerce.identity.application.port.out.user.UserPersistencePort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.model.IdentityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Service for managing user profile operations.
 * Handles profile retrieval, display name updates, and avatar management.
 * 
 * <p>
 * Business Rules:
 * <ul>
 * <li>Only active users can access or update their profiles</li>
 * <li>Display names must not be blank</li>
 * <li>Avatar URLs must be valid HTTP/HTTPS URLs</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService implements
        GetMyProfileInputPort,
        UpdateDisplayNameInputPort,
        UpdateAvatarInputPort {

    private final UserPersistencePort userPersistencePort;

    @Override
    public UserProfileView execute(GetMyProfileQuery query) {
        log.debug("Fetching profile for user: {}", query.userId());
        return getMyProfile(query.userId());
    }

    @Override
    public UserProfileView execute(UpdateDisplayNameCommand command) {
        log.info("Updating display name for user: {}", command.userId());
        return updateDisplayName(command.userId(), command.displayName());
    }

    @Override
    public UserProfileView execute(UpdateAvatarCommand command) {
        log.info("Updating avatar for user: {}", command.userId());
        return updateAvatar(command.userId(), command.avatarUrl());
    }

    /**
     * Get the profile of the current user.
     *
     * @param userId the user ID
     * @return the user profile view
     * @throws IdentityException if user not found or inactive
     */
    public UserProfileView getMyProfile(String userId) {
        IdentityUser user = getActiveUser(userId);
        return toProfileView(user);
    }

    /**
     * Update the display name of the current user.
     *
     * @param userId      the user ID
     * @param displayName the new display name
     * @return the updated user profile view
     * @throws IdentityException if user not found, inactive, or display name is
     *                           invalid
     */
    public UserProfileView updateDisplayName(String userId, String displayName) {
        IdentityUser user = getActiveUser(userId);
        user.updateDisplayName(displayName);
        IdentityUser saved = userPersistencePort.save(user);
        log.info("Display name updated for user: {}", userId);
        return toProfileView(saved);
    }

    /**
     * Update the avatar URL of the current user.
     *
     * @param userId    the user ID
     * @param avatarUrl the new avatar URL
     * @return the updated user profile view
     * @throws IdentityException if user not found, inactive, or avatar URL is
     *                           invalid
     */
    public UserProfileView updateAvatar(String userId, String avatarUrl) {
        IdentityUser user = getActiveUser(userId);
        if (avatarUrl == null || avatarUrl.isBlank() || !avatarUrl.startsWith("http")) {
            log.warn("Invalid avatar URL provided for user: {}", userId);
            throw new IdentityException(IdentityErrorCode.AVATAR_URL_INVALID);
        }
        user.updateAvatar(avatarUrl.trim());
        IdentityUser saved = userPersistencePort.save(user);
        log.info("Avatar updated for user: {}", userId);
        return toProfileView(saved);
    }

    private IdentityUser getActiveUser(String userId) {
        IdentityUser user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new IdentityException(IdentityErrorCode.USER_INACTIVE);
        }
        return user;
    }

    private UserProfileView toProfileView(IdentityUser user) {
        return new UserProfileView(
                user.getUserId(),
                user.getEmail(),
                user.getPhone(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                user.getStatus().name(),
                user.getEmailVerifiedAt(),
                user.getPhoneVerifiedAt(),
                user.getCreatedAt());
    }
}
