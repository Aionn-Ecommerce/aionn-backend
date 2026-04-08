package com.ecommerce.identity.application.usecase.preference;

import com.ecommerce.identity.application.dto.preference.command.UpdateGeneralPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.result.UserPreferenceResult;
import com.ecommerce.identity.application.port.in.preference.UpdateGeneralPreferenceInputPort;
import com.ecommerce.identity.application.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UpdateGeneralPreferenceUseCase implements UpdateGeneralPreferenceInputPort {

    private final PreferenceService preferenceService;

    @Override
    @Transactional
    public UserPreferenceResult execute(UpdateGeneralPreferenceCommand command) {
        return preferenceService.updateGeneral(command);
    }
}
