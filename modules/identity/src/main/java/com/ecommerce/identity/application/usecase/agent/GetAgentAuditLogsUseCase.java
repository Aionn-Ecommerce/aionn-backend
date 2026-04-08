package com.ecommerce.identity.application.usecase.agent;

import com.ecommerce.identity.application.dto.agent.query.GetAgentAuditLogsQuery;
import com.ecommerce.identity.application.dto.agent.result.AgentAuditLogResult;
import com.ecommerce.identity.application.mapper.AgentResultMapper;
import com.ecommerce.identity.application.port.in.agent.GetAgentAuditLogsQueryPort;
import com.ecommerce.identity.application.service.AgentService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetAgentAuditLogsUseCase implements GetAgentAuditLogsQueryPort {

    private final AgentService agentService;
    private final AgentResultMapper agentResultMapper;

    @Override
    public List<AgentAuditLogResult> execute(GetAgentAuditLogsQuery query) {
        return agentService.getAgentAuditLogs(query.ownerUserId(), query.agentId()).stream()
                .map(agentResultMapper::toAuditLogResult)
                .toList();
    }
}
