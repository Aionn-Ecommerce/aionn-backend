package com.ecommerce.identity.application.port.in.agent;

import com.ecommerce.identity.application.dto.agent.result.AgentIdentityResult;
import com.ecommerce.identity.application.dto.agent.command.SuspendAgentCommand;

public interface SuspendAgentInputPort {
    AgentIdentityResult execute(SuspendAgentCommand command);
}
