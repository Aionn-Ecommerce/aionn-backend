package com.ecommerce.identity.application.dto.admin;

import java.util.Set;

public record UserRolesResult(
        Set<String> roles
) {
}
