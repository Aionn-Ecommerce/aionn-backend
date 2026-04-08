package com.ecommerce.identity.adapter.rest.dto.admin;

import com.ecommerce.identity.domain.valueobject.UserRole;
import com.ecommerce.identity.domain.valueobject.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record UserDetailResponse(
                String userId,
                String email,
                String phone,
                String displayName,
                Set<UserRole> roles,
                UserStatus status,
                LocalDateTime createdAt,
                LocalDateTime emailVerifiedAt,
                LocalDateTime phoneVerifiedAt) {
}


