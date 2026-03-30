package com.ecommerce.identity.application.port.in.agent;

import com.ecommerce.identity.application.dto.agent.RevokeAgentCommand;

public interface RevokeAgentInputPort {
    void execute(RevokeAgentCommand command);
}