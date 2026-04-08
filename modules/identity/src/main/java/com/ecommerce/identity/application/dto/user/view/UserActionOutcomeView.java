package com.ecommerce.identity.application.dto.user.view;

import com.ecommerce.identity.application.dto.user.view.UserProfileView;

public record UserActionOutcomeView(
                String action,
                String message,
                UserProfileView profile) {
}


