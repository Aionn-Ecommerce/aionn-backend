package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.preference.UpdateAiPrivacyPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.UpdateGeneralPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.UpdateNotificationPreferenceCommand;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.infrastructure.persistence.entity.UserPreferenceEntity;
import com.ecommerce.identity.infrastructure.persistence.repository.preference.UserPreferenceRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;

    @Transactional
    public UserPreferenceEntity updateGeneral(UpdateGeneralPreferenceCommand command) {
        UserPreferenceEntity preference = getOrCreate(command.userId());
        preference.setLanguage(command.language());
        preference.setCurrency(command.currency());
        preference.setTimezone(command.timezone());
        preference.setTheme(command.theme());
        return preferenceRepository.save(preference);
    }

    @Transactional
    public UserPreferenceEntity updateNotifications(UpdateNotificationPreferenceCommand command) {
        UserPreferenceEntity preference = getOrCreate(command.userId());
        preference.setNotificationSettings(command.notificationSettingsJson());
        return preferenceRepository.save(preference);
    }

    @Transactional
    public UserPreferenceEntity updateAiPrivacy(UpdateAiPrivacyPreferenceCommand command) {
        UserPreferenceEntity preference = getOrCreate(command.userId());
        preference.setAiPrivacySettings(command.aiPrivacySettingsJson());
        return preferenceRepository.save(preference);
    }

    @Transactional(readOnly = true)
    public UserPreferenceEntity get(String userId) {
        return getOrCreate(userId);
    }

    private UserPreferenceEntity getOrCreate(String userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        return preferenceRepository.findById(userId).orElseGet(() -> UserPreferenceEntity.builder()
                .userId(userId)
                .user(user)
                .build());
    }
}
