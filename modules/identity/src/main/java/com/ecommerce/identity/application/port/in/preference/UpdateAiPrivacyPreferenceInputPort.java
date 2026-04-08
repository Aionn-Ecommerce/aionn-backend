package com.ecommerce.identity.application.port.in.preference;

import com.ecommerce.identity.application.dto.preference.command.UpdateAiPrivacyPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.result.UserPreferenceResult;

public interface UpdateAiPrivacyPreferenceInputPort {
    UserPreferenceResult execute(UpdateAiPrivacyPreferenceCommand command);
}

