package com.ecommerce.identity.application.port.in.preference;

import com.ecommerce.identity.application.dto.preference.result.UserPreferenceResult;

public interface GetUserPreferenceQueryPort {
    UserPreferenceResult execute(String userId);
}

