package com.ecommerce.identity.adapter.rest.dto.admin;

import com.ecommerce.identity.domain.valueobject.UserRole;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateRolesRequest(
                @NotEmpty(message = "Roles must not be empty") Set<UserRole> roles) {
}
