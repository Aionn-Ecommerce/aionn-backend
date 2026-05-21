package com.aionn.identity.application.dto.user.command;

import com.aionn.sharedkernel.application.command.Command;

public record ChangePhoneCommand(
                String userId,
                String action,
                String newPhone,
                String otpCode) implements Command {
}

