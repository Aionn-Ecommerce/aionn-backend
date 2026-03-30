package com.ecommerce.identity.application.dto.user;

import com.ecommerce.sharedkernel.application.command.Command;

public record ChangeEmailCommand(
        String userId,
        String action,
        String newEmail,
        String otpCode) implements Command {
}
