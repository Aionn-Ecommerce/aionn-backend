package com.ecommerce.identity.application.dto.admin.command;

import java.util.Set;

public record RemoveUserRolesCommand(
        String userId,
        Set<String> roles) {
}
