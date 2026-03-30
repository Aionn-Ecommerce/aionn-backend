package com.ecommerce.identity.application.port.in.preference;

import com.ecommerce.identity.application.dto.preference.UpdateGeneralPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.UserPreferenceResult;

public interface UpdateGeneralPreferenceInputPort {
    UserPreferenceResult execute(UpdateGeneralPreferenceCommand command);
}