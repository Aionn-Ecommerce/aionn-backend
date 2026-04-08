package com.ecommerce.identity.application.dto.admin.command;

public record UpdateUserStatusCommand(
                String userId,
                String status) {
}


