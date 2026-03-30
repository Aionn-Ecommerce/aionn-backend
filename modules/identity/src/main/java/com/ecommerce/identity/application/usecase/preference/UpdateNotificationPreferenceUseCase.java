package com.ecommerce.identity.application.usecase.preference;

import com.ecommerce.identity.application.dto.preference.UpdateNotificationPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.UserPreferenceResult;
import com.ecommerce.identity.application.port.in.preference.UpdateNotificationPreferenceInputPort;
import com.ecommerce.identity.application.service.PreferenceService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateNotificationPreferenceUseCase implements UpdateNotificationPreferenceInputPort {

    private final PreferenceService preferenceService;

    @Override
    public UserPreferenceResult execute(UpdateNotificationPreferenceCommand command) {
        var result = preferenceService.updateNotifications(command);
        return new UserPreferenceResult(
                result.getUserId(),
                result.getLanguage(),
                result.getCurrency(),
                result.getTimezone(),
                result.getTheme(),
                result.getNotificationSettings(),
                result.getAiPrivacySettings(),
                result.getUpdatedAt());
    }
}
