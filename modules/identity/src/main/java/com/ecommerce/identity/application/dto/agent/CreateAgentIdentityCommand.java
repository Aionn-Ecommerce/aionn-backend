package com.ecommerce.identity.application.dto.agent;

import com.ecommerce.sharedkernel.application.command.Command;

public record CreateAgentIdentityCommand(
                String ownerUserId,
                String agentName) implements Command {
}
