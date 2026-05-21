package com.aionn.identity.application.dto.user.view;

import com.aionn.identity.application.dto.user.view.UserProfileView;

public record UserActionOutcomeView(
                String action,
                String message,
                UserProfileView profile) {
}



