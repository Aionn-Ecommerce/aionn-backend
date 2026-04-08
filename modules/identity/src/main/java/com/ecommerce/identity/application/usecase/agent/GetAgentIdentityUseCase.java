package com.ecommerce.identity.application.usecase.agent;

import com.ecommerce.identity.application.dto.agent.query.GetAgentIdentityQuery;
import com.ecommerce.identity.application.dto.agent.result.AgentIdentityResult;
import com.ecommerce.identity.application.mapper.AgentResultMapper;
import com.ecommerce.identity.application.port.in.agent.GetAgentIdentityQueryPort;
import com.ecommerce.identity.application.service.AgentService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetAgentIdentityUseCase implements GetAgentIdentityQueryPort {

    private final AgentService agentService;
    private final AgentResultMapper agentResultMapper;

    @Override
    public AgentIdentityResult execute(GetAgentIdentityQuery query) {
        var entity = agentService.get(query.userId(), query.agentId());
        return agentResultMapper.toIdentityResult(entity);
    }
}

