package com.ecommerce.identity.application.usecase.agent;

import com.ecommerce.identity.application.dto.agent.result.AgentIdentityResult;
import com.ecommerce.identity.application.dto.agent.command.CreateAgentIdentityCommand;
import com.ecommerce.identity.application.mapper.AgentResultMapper;
import com.ecommerce.identity.application.port.in.agent.CreateAgentIdentityInputPort;
import com.ecommerce.identity.application.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class CreateAgentIdentityUseCase implements CreateAgentIdentityInputPort {

    private final AgentService agentService;
    private final AgentResultMapper agentResultMapper;

    @Override
    @Transactional
    public AgentIdentityResult execute(CreateAgentIdentityCommand command) {
        var result = agentService.create(command.ownerUserId());
        return agentResultMapper.toIdentityResult(result);
    }
}
