package com.ecommerce.identity.application.dto.user;

import com.ecommerce.sharedkernel.application.command.Command;

public record UpdateAvatarCommand(
        String userId,
        String avatarUrl) implements Command {
}
