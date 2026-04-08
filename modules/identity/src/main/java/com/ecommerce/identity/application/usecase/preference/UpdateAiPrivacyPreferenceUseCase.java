package com.ecommerce.identity.application.usecase.preference;

import com.ecommerce.identity.application.dto.preference.command.UpdateAiPrivacyPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.result.UserPreferenceResult;
import com.ecommerce.identity.application.port.in.preference.UpdateAiPrivacyPreferenceInputPort;
import com.ecommerce.identity.application.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UpdateAiPrivacyPreferenceUseCase implements UpdateAiPrivacyPreferenceInputPort {

    private final PreferenceService preferenceService;

    @Override
    @Transactional
    public UserPreferenceResult execute(UpdateAiPrivacyPreferenceCommand command) {
        return preferenceService.updateAiPrivacy(command);
    }
}
