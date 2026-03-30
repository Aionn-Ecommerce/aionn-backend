package com.ecommerce.identity.application.port.in.agent;

import com.ecommerce.identity.application.dto.agent.UpdateAgentPermissionsCommand;
import com.ecommerce.identity.application.dto.agent.AgentIdentityResult;

public interface UpdateAgentPermissionsInputPort {
    AgentIdentityResult execute(UpdateAgentPermissionsCommand command);
}