package com.ecommerce.identity.application.usecase.agent;

import com.ecommerce.identity.application.dto.agent.RevokeAgentCommand;
import com.ecommerce.identity.application.port.in.agent.RevokeAgentInputPort;
import com.ecommerce.identity.application.service.AgentService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RevokeAgentUseCase implements RevokeAgentInputPort {

    private final AgentService agentService;

    @Override
    public void execute(RevokeAgentCommand command) {
        agentService.revoke(command.ownerUserId(), command.agentId());
    }
}
