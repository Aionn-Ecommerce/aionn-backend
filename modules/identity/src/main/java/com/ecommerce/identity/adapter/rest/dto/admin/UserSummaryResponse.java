package com.ecommerce.identity.adapter.rest.dto.admin;

import com.ecommerce.identity.domain.valueobject.UserRole;
import com.ecommerce.identity.domain.valueobject.UserStatus;

public record UserSummaryResponse(
                String userId,
                String email,
                String displayName,
                UserStatus status,
                UserRole primaryRole) {
}


