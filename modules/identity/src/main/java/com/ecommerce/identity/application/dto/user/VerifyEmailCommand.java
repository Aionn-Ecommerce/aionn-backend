package com.ecommerce.identity.application.dto.user;

import com.ecommerce.sharedkernel.application.command.Command;

public record VerifyEmailCommand(
        String userId,
        String action,
        String otpCode) implements Command {
}
