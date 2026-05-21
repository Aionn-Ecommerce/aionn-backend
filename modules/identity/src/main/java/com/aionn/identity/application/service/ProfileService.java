package com.aionn.identity.application.service;

import com.aionn.identity.application.dto.user.command.UpdateAvatarCommand;
import com.aionn.identity.application.dto.user.command.UpdateDisplayNameCommand;
import com.aionn.identity.application.dto.user.query.GetMyProfileQuery;
import com.aionn.identity.application.dto.user.view.UserProfileView;
import com.aionn.identity.application.port.out.user.UserPersistencePort;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.domain.model.IdentityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

/**
 * Service for user profile read/update operations. Methods are exposed to use
 * cases via plain Java calls; the input ports themselves are not implemented
 * here so that the use case layer can wrap each invocation in its own
 * transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final int MAX_DISPLAY_NAME_LENGTH = 100;
    private static final int MAX_AVATAR_URL_LENGTH = 2048;

    private final UserPersistencePort userPersistencePort;

    public UserProfileView getMyProfile(GetMyProfileQuery query) {
        log.debug("Fetching profile for user: {}", query.userId());
        return toProfileView(getActiveUser(query.userId()));
    }

    public UserProfileView updateDisplayName(UpdateDisplayNameCommand command) {
        log.info("Updating display name for user: {}", command.userId());
        if (command.displayName() != null && command.displayName().length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new IdentityException(IdentityErrorCode.INVALID_DISPLAY_NAME,
                    "Display name exceeds " + MAX_DISPLAY_NAME_LENGTH + " characters");
        }
        IdentityUser user = getActiveUser(command.userId());
        user.updateDisplayName(command.displayName());
        return toProfileView(userPersistencePort.save(user));
    }

    public UserProfileView updateAvatar(UpdateAvatarCommand command) {
        log.info("Updating avatar for user: {}", command.userId());
        validateAvatarUrl(command.avatarUrl());
        IdentityUser user = getActiveUser(command.userId());
        user.updateAvatar(command.avatarUrl().trim());
        return toProfileView(userPersistencePort.save(user));
    }

    private IdentityUser getActiveUser(String userId) {
        IdentityUser user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new IdentityException(IdentityErrorCode.USER_INACTIVE);
        }
        return user;
    }

    /**
     * Avatar URL validation: must be a syntactically valid HTTP(S) URI with a
     * non-empty host. Rejects schemes other than http/https to mitigate SSRF
     * and prevents trivially malformed strings (the previous implementation
     * accepted anything that started with "http").
     */
    private static void validateAvatarUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            throw new IdentityException(IdentityErrorCode.AVATAR_URL_INVALID);
        }
        if (avatarUrl.length() > MAX_AVATAR_URL_LENGTH) {
            throw new IdentityException(IdentityErrorCode.AVATAR_URL_INVALID,
                    "Avatar URL exceeds maximum length");
        }
        try {
            URI uri = new URI(avatarUrl.trim());
            String scheme = uri.getScheme();
            if (scheme == null
                    || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null
                    || uri.getHost().isBlank()) {
                throw new IdentityException(IdentityErrorCode.AVATAR_URL_INVALID);
            }
        } catch (URISyntaxException ex) {
            throw new IdentityException(IdentityErrorCode.AVATAR_URL_INVALID);
        }
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

