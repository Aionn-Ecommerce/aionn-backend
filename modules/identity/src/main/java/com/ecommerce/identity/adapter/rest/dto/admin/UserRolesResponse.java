package com.ecommerce.identity.adapter.rest.dto.admin;

import com.ecommerce.identity.domain.valueobject.UserRole;

import java.util.Set;

public record UserRolesResponse(
                Set<UserRole> roles) {
}
