package com.ecommerce.identity.application.usecase.agent;

import com.ecommerce.identity.application.dto.agent.result.AgentIdentityResult;
import com.ecommerce.identity.application.dto.agent.command.SuspendAgentCommand;
import com.ecommerce.identity.application.mapper.AgentResultMapper;
import com.ecommerce.identity.application.port.in.agent.SuspendAgentInputPort;
import com.ecommerce.identity.application.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class SuspendAgentUseCase implements SuspendAgentInputPort {

    private final AgentService agentService;
    private final AgentResultMapper agentResultMapper;

    @Override
    @Transactional
    public AgentIdentityResult execute(SuspendAgentCommand command) {
        var result = agentService.suspend(command.ownerUserId(), command.agentId());
        return agentResultMapper.toIdentityResult(result);
    }
}
