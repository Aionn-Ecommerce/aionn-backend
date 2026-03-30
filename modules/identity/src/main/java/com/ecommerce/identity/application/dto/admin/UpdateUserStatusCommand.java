package com.ecommerce.identity.application.dto.admin;

public record UpdateUserStatusCommand(
        String userId,
        String status
) {
}
