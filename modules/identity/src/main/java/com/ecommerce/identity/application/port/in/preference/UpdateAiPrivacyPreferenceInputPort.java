package com.ecommerce.identity.application.port.in.preference;

import com.ecommerce.identity.application.dto.preference.UpdateAiPrivacyPreferenceCommand;
import com.ecommerce.identity.application.dto.preference.UserPreferenceResult;

public interface UpdateAiPrivacyPreferenceInputPort {
    UserPreferenceResult execute(UpdateAiPrivacyPreferenceCommand command);
}