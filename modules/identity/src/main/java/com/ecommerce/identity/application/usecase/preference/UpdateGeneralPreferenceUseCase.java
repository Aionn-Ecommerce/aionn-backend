package com.ecommerce.identity.application.usecase.preference;

import com.ecommerce.identity.application.dto.preference.UpdateGeneralPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.UserPreferenceResult;
import com.ecommerce.identity.application.port.in.preference.UpdateGeneralPreferenceInputPort;
import com.ecommerce.identity.application.service.PreferenceService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateGeneralPreferenceUseCase implements UpdateGeneralPreferenceInputPort {

    private final PreferenceService preferenceService;

    @Override
    public UserPreferenceResult execute(UpdateGeneralPreferenceCommand command) {
        var result = preferenceService.updateGeneral(command);
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
