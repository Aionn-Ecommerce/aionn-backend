package com.ecommerce.identity.application.port.in.agent;

import com.ecommerce.identity.application.dto.agent.SuspendAgentCommand;
import com.ecommerce.identity.application.dto.agent.AgentIdentityResult;

public interface SuspendAgentInputPort {
    AgentIdentityResult execute(SuspendAgentCommand command);
}