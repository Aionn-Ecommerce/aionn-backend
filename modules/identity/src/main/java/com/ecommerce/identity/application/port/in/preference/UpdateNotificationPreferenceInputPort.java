package com.ecommerce.identity.application.port.in.preference;

import com.ecommerce.identity.application.dto.preference.command.UpdateNotificationPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.result.UserPreferenceResult;

public interface UpdateNotificationPreferenceInputPort {
    UserPreferenceResult execute(UpdateNotificationPreferenceCommand command);
}

