package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.application.dto.preference.result.UserPreferenceResult;
import com.ecommerce.identity.infrastructure.persistence.entity.UserPreferenceEntity;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserPreferenceDomainMapperImpl implements UserPreferenceDomainMapper {

    @Override
    public UserPreferenceEntity toEntity(UserPreferenceResult result) {
        if ( result == null ) {
            return null;
        }

        UserPreferenceEntity.UserPreferenceEntityBuilder userPreferenceEntity = UserPreferenceEntity.builder();

        userPreferenceEntity.aiPrivacySettings( result.aiPrivacySettings() );
        userPreferenceEntity.currency( result.currency() );
        userPreferenceEntity.language( result.language() );
        userPreferenceEntity.notificationSettings( result.notificationSettings() );
        userPreferenceEntity.theme( result.theme() );
        userPreferenceEntity.timezone( result.timezone() );
        userPreferenceEntity.updatedAt( result.updatedAt() );
        userPreferenceEntity.userId( result.userId() );

        return userPreferenceEntity.build();
    }

    @Override
    public UserPreferenceResult toResult(UserPreferenceEntity entity) {
        if ( entity == null ) {
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

        userId = entity.getUserId();
        language = entity.getLanguage();
        currency = entity.getCurrency();
        timezone = entity.getTimezone();
        theme = entity.getTheme();
        notificationSettings = entity.getNotificationSettings();
        aiPrivacySettings = entity.getAiPrivacySettings();
        updatedAt = entity.getUpdatedAt();

        UserPreferenceResult userPreferenceResult = new UserPreferenceResult( userId, language, currency, timezone, theme, notificationSettings, aiPrivacySettings, updatedAt );

        return userPreferenceResult;
    }
}
