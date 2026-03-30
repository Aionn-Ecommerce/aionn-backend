package com.ecommerce.identity.application.dto.auth;

import com.ecommerce.sharedkernel.application.command.Command;

public record UnlinkSocialCommand(
        String userId,
        String provider) implements Command {
}
