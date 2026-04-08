package com.ecommerce.identity.application.dto.admin.command;

import java.util.Set;

public record UpdateUserRolesCommand(
                String userId,
                Set<String> roles) {
}


