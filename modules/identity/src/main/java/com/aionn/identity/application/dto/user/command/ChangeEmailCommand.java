package com.aionn.identity.application.dto.user.command;

import com.aionn.sharedkernel.application.command.Command;

public record ChangeEmailCommand(
                String userId,
                String action,
                String newEmail,
                String otpCode) implements Command {
}



