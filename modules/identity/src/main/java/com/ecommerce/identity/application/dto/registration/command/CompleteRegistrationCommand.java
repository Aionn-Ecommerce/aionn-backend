package com.ecommerce.identity.application.dto.registration.command;

import com.ecommerce.sharedkernel.application.command.Command;

public record CompleteRegistrationCommand(
                String regId,
                String password,
                String username,
                String verificationToken,
                String ipAddress,
                String userAgent) implements Command {
}
