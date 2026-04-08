package com.ecommerce.identity.application.usecase.preference;

import com.ecommerce.identity.application.dto.preference.command.UpdateNotificationPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.result.UserPreferenceResult;
import com.ecommerce.identity.application.port.in.preference.UpdateNotificationPreferenceInputPort;
import com.ecommerce.identity.application.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UpdateNotificationPreferenceUseCase implements UpdateNotificationPreferenceInputPort {

    private final PreferenceService preferenceService;

    @Override
    @Transactional
    public UserPreferenceResult execute(UpdateNotificationPreferenceCommand command) {
        return preferenceService.updateNotifications(command);
    }
}
