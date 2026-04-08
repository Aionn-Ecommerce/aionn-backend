package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.preference.command.UpdateAiPrivacyPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.command.UpdateGeneralPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.command.UpdateNotificationPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.result.UserPreferenceResult;
import com.ecommerce.identity.application.port.out.preference.UserPreferencePersistencePort;
import com.ecommerce.identity.application.port.out.user.UserPersistencePort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final UserPersistencePort userPersistencePort;
    private final UserPreferencePersistencePort preferencePersistencePort;

    public UserPreferenceResult updateGeneral(UpdateGeneralPreferenceCommand command) {
        log.info("Updating general preferences for user: {}", command.userId());
        UserPreferenceResult preference = getOrCreate(command.userId());

        UserPreferenceResult updated = new UserPreferenceResult(
                preference.userId(),
                command.language(),
                command.currency(),
                command.timezone(),
                command.theme(),
                preference.notificationSettings(),
                preference.aiPrivacySettings(),
                LocalDateTime.now());

        return preferencePersistencePort.save(updated);
    }

    public UserPreferenceResult updateNotifications(UpdateNotificationPreferenceCommand command) {
        log.info("Updating notification preferences for user: {}", command.userId());
        UserPreferenceResult preference = getOrCreate(command.userId());

        UserPreferenceResult updated = new UserPreferenceResult(
                preference.userId(),
                preference.language(),
                preference.currency(),
                preference.timezone(),
                preference.theme(),
                command.notificationSettingsJson(),
                preference.aiPrivacySettings(),
                LocalDateTime.now());

        return preferencePersistencePort.save(updated);
    }

    public UserPreferenceResult updateAiPrivacy(UpdateAiPrivacyPreferenceCommand command) {
        log.info("Updating AI privacy preferences for user: {}", command.userId());
        UserPreferenceResult preference = getOrCreate(command.userId());

        UserPreferenceResult updated = new UserPreferenceResult(
                preference.userId(),
                preference.language(),
                preference.currency(),
                preference.timezone(),
                preference.theme(),
                preference.notificationSettings(),
                command.aiPrivacySettingsJson(),
                LocalDateTime.now());

        return preferencePersistencePort.save(updated);
    }

    public UserPreferenceResult get(String userId) {
        log.debug("Getting preferences for user: {}", userId);
        return getOrCreate(userId);
    }

    /**
     * Gets or creates user preferences.
     * Race condition is prevented by database-level unique constraint on user_id
     * (PRIMARY KEY).
     * If two concurrent requests attempt to create preferences, one will succeed
     * and the other will fail with a duplicate key violation. We retry the read
     * operation to fetch the successfully created preference.
     */
    private UserPreferenceResult getOrCreate(String userId) {
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));

        Optional<UserPreferenceResult> existing = preferencePersistencePort.findById(userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Try to create default preferences
        try {
            return preferencePersistencePort.createDefault(userId);
        } catch (Exception e) {
            // If creation fails due to duplicate key (race condition), retry the read
            log.debug("Concurrent preference creation detected for user: {}, retrying read", userId);
            return preferencePersistencePort.findById(userId)
                    .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        }
    }
}
