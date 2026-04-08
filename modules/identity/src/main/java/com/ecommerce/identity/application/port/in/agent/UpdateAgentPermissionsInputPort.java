package com.ecommerce.identity.application.port.in.agent;

import com.ecommerce.identity.application.dto.agent.result.AgentIdentityResult;
import com.ecommerce.identity.application.dto.agent.command.UpdateAgentPermissionsCommand;

public interface UpdateAgentPermissionsInputPort {
    AgentIdentityResult execute(UpdateAgentPermissionsCommand command);
}
