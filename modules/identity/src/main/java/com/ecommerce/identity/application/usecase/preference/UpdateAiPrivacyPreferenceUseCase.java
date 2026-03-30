package com.ecommerce.identity.application.usecase.preference;

import com.ecommerce.identity.application.dto.preference.UpdateAiPrivacyPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.UserPreferenceResult;
import com.ecommerce.identity.application.port.in.preference.UpdateAiPrivacyPreferenceInputPort;
import com.ecommerce.identity.application.service.PreferenceService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateAiPrivacyPreferenceUseCase implements UpdateAiPrivacyPreferenceInputPort {

    private final PreferenceService preferenceService;

    @Override
    public UserPreferenceResult execute(UpdateAiPrivacyPreferenceCommand command) {
        var result = preferenceService.updateAiPrivacy(command);
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
