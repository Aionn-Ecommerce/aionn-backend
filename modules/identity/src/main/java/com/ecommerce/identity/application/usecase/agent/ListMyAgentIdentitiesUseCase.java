package com.ecommerce.identity.application.usecase.agent;

import com.ecommerce.identity.application.dto.agent.AgentIdentityResult;
import com.ecommerce.identity.application.port.in.agent.ListMyAgentIdentitiesQueryPort;
import com.ecommerce.identity.application.service.AgentService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListMyAgentIdentitiesUseCase implements ListMyAgentIdentitiesQueryPort {

    private final AgentService agentService;

    @Override
    public List<AgentIdentityResult> execute(String userId) {
        return agentService.listMy(userId).stream()
                .map(AgentResultMapper::toIdentityResult)
                .toList();
    }
}
