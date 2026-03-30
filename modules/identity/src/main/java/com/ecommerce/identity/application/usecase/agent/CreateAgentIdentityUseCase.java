package com.ecommerce.identity.application.usecase.agent;

import com.ecommerce.identity.application.dto.agent.AgentIdentityResult;
import com.ecommerce.identity.application.dto.agent.CreateAgentIdentityCommand;
import com.ecommerce.identity.application.port.in.agent.CreateAgentIdentityInputPort;
import com.ecommerce.identity.application.service.AgentService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateAgentIdentityUseCase implements CreateAgentIdentityInputPort {

    private final AgentService agentService;

    @Override
    public AgentIdentityResult execute(CreateAgentIdentityCommand command) {
        var result = agentService.create(command.ownerUserId());
        return AgentResultMapper.toIdentityResult(result);
    }
}
