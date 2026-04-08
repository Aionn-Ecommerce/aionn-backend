package com.ecommerce.identity.application.port.in.agent;

import com.ecommerce.identity.application.dto.agent.command.CreateAgentIdentityCommand;
import com.ecommerce.identity.application.dto.agent.result.AgentIdentityResult;

public interface CreateAgentIdentityInputPort {
    AgentIdentityResult execute(CreateAgentIdentityCommand command);
}
