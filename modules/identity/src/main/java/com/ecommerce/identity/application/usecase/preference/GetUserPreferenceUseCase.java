package com.ecommerce.identity.application.usecase.preference;

import com.ecommerce.identity.application.dto.preference.result.UserPreferenceResult;
import com.ecommerce.identity.application.port.in.preference.GetUserPreferenceQueryPort;
import com.ecommerce.identity.application.service.PreferenceService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetUserPreferenceUseCase implements GetUserPreferenceQueryPort {

    private final PreferenceService preferenceService;

    @Override
    public UserPreferenceResult execute(String userId) {
        return preferenceService.get(userId);
    }
}
