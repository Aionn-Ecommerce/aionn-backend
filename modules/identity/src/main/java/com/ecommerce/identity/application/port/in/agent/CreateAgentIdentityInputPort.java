package com.ecommerce.identity.application.port.in.agent;

import com.ecommerce.identity.application.dto.agent.CreateAgentIdentityCommand;
import com.ecommerce.identity.application.dto.agent.AgentIdentityResult;

public interface CreateAgentIdentityInputPort {
    AgentIdentityResult execute(CreateAgentIdentityCommand command);
}