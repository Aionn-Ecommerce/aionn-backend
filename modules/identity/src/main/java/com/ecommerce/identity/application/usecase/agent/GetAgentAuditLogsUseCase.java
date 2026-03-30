package com.ecommerce.identity.application.usecase.agent;

import com.ecommerce.identity.application.dto.agent.AgentAuditLogResult;
import com.ecommerce.identity.application.dto.agent.GetAgentAuditLogsQuery;
import com.ecommerce.identity.application.port.in.agent.GetAgentAuditLogsQueryPort;
import com.ecommerce.identity.application.service.AgentService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetAgentAuditLogsUseCase implements GetAgentAuditLogsQueryPort {

    private final AgentService agentService;

    @Override
    public List<AgentAuditLogResult> execute(GetAgentAuditLogsQuery query) {
        return agentService.getAgentAuditLogs(query.ownerUserId(), query.agentId()).stream()
                .map(AgentResultMapper::toAuditLogResult)
                .toList();
    }
}
