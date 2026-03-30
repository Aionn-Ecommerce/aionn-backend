package com.ecommerce.identity.application.port.in.preference;

import com.ecommerce.identity.application.dto.preference.UserPreferenceResult;

public interface GetUserPreferenceQueryPort {
    UserPreferenceResult execute(String userId);
}