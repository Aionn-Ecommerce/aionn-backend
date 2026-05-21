package com.aionn.identity.application.dto.user.command;

import com.aionn.sharedkernel.application.command.Command;

public record VerifyEmailCommand(
                String userId,
                String action,
                String otpCode) implements Command {
}



