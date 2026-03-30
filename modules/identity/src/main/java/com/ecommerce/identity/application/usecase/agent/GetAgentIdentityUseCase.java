package com.ecommerce.identity.application.usecase.agent;

import com.ecommerce.identity.application.dto.agent.GetAgentIdentityQuery;
import com.ecommerce.identity.application.dto.agent.AgentIdentityResult;
import com.ecommerce.identity.application.port.in.agent.GetAgentIdentityQueryPort;
import com.ecommerce.identity.application.service.AgentService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetAgentIdentityUseCase implements GetAgentIdentityQueryPort {

    private final AgentService agentService;

    @Override
    public AgentIdentityResult execute(GetAgentIdentityQuery query) {
        var entity = agentService.get(query.userId(), query.agentId());
        return AgentResultMapper.toIdentityResult(entity);
    }
}
