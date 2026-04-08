package com.ecommerce.identity.application.port.in.preference;

import com.ecommerce.identity.application.dto.preference.command.UpdateGeneralPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.result.UserPreferenceResult;

public interface UpdateGeneralPreferenceInputPort {
    UserPreferenceResult execute(UpdateGeneralPreferenceCommand command);
}

