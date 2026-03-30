package com.ecommerce.identity.adapter.rest.mapper.preference;

import com.ecommerce.identity.adapter.rest.dto.preference.AiPrivacyPreferenceRequest;
import com.ecommerce.identity.adapter.rest.dto.preference.GeneralPreferenceRequest;
import com.ecommerce.identity.adapter.rest.dto.preference.NotificationPreferenceRequest;
import com.ecommerce.identity.adapter.rest.dto.preference.UserPreferenceResponse;
import com.ecommerce.identity.application.dto.preference.UpdateAiPrivacyPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.UpdateGeneralPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.UpdateNotificationPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.UserPreferenceResult;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-29T16:48:41+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserPreferenceDtoMapperImpl implements UserPreferenceDtoMapper {

    @Override
    public UpdateGeneralPreferenceCommand toUpdateGeneralCommand(String userId, GeneralPreferenceRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        String language = null;
        String currency = null;
        String timezone = null;
        String theme = null;
        if ( request != null ) {
            language = request.language();
            currency = request.currency();
            timezone = request.timezone();
            theme = request.theme();
        }
        String userId1 = null;
        userId1 = userId;

        UpdateGeneralPreferenceCommand updateGeneralPreferenceCommand = new UpdateGeneralPreferenceCommand( userId1, language, currency, timezone, theme );

        return updateGeneralPreferenceCommand;
    }

    @Override
    public UpdateNotificationPreferenceCommand toUpdateNotificationsCommand(String userId, NotificationPreferenceRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        String notificationSettingsJson = null;
        if ( request != null ) {
            notificationSettingsJson = request.notificationSettingsJson();
        }
        String userId1 = null;
        userId1 = userId;

        UpdateNotificationPreferenceCommand updateNotificationPreferenceCommand = new UpdateNotificationPreferenceCommand( userId1, notificationSettingsJson );

        return updateNotificationPreferenceCommand;
    }

    @Override
    public UpdateAiPrivacyPreferenceCommand toUpdateAiPrivacyCommand(String userId, AiPrivacyPreferenceRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        String aiPrivacySettingsJson = null;
        if ( request != null ) {
            aiPrivacySettingsJson = request.aiPrivacySettingsJson();
        }
        String userId1 = null;
        userId1 = userId;

        UpdateAiPrivacyPreferenceCommand updateAiPrivacyPreferenceCommand = new UpdateAiPrivacyPreferenceCommand( userId1, aiPrivacySettingsJson );

        return updateAiPrivacyPreferenceCommand;
    }

    @Override
    public UserPreferenceResponse toResponse(UserPreferenceResult preference) {
        if ( preference == null ) {
            return null;
        }

        String userId = null;
        String language = null;
        String currency = null;
        String timezone = null;
        String theme = null;
        String notificationSettings = null;
        String aiPrivacySettings = null;
        LocalDateTime updatedAt = null;

        userId = preference.userId();
        language = preference.language();
        currency = preference.currency();
        timezone = preference.timezone();
        theme = preference.theme();
        notificationSettings = preference.notificationSettings();
        aiPrivacySettings = preference.aiPrivacySettings();
        updatedAt = preference.updatedAt();

        UserPreferenceResponse userPreferenceResponse = new UserPreferenceResponse( userId, language, currency, timezone, theme, notificationSettings, aiPrivacySettings, updatedAt );

        return userPreferenceResponse;
    }
}
