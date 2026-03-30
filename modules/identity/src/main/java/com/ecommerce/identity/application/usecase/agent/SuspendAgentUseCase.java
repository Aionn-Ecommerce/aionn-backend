package com.ecommerce.identity.application.usecase.agent;

import com.ecommerce.identity.application.dto.agent.AgentIdentityResult;
import com.ecommerce.identity.application.dto.agent.SuspendAgentCommand;
import com.ecommerce.identity.application.port.in.agent.SuspendAgentInputPort;
import com.ecommerce.identity.application.service.AgentService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SuspendAgentUseCase implements SuspendAgentInputPort {

    private final AgentService agentService;

    @Override
    public AgentIdentityResult execute(SuspendAgentCommand command) {
        var result = agentService.suspend(command.ownerUserId(), command.agentId());
        return AgentResultMapper.toIdentityResult(result);
    }
}
