package com.ecommerce.identity.application.usecase.agent;

import com.ecommerce.identity.application.dto.agent.result.AgentIdentityResult;
import com.ecommerce.identity.application.mapper.AgentResultMapper;
import com.ecommerce.identity.application.port.in.agent.ListMyAgentIdentitiesQueryPort;
import com.ecommerce.identity.application.service.AgentService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListMyAgentIdentitiesUseCase implements ListMyAgentIdentitiesQueryPort {

    private final AgentService agentService;
    private final AgentResultMapper agentResultMapper;

    @Override
    public List<AgentIdentityResult> execute(String userId) {
        return agentService.listMy(userId).stream()
                .map(agentResultMapper::toIdentityResult)
                .toList();
    }
}

