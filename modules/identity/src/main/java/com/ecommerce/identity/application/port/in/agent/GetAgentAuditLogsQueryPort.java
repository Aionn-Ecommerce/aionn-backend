package com.ecommerce.identity.application.port.in.agent;

import com.ecommerce.identity.application.dto.agent.query.GetAgentAuditLogsQuery;
import com.ecommerce.identity.application.dto.agent.result.AgentAuditLogResult;

import java.util.List;

public interface GetAgentAuditLogsQueryPort {
    List<AgentAuditLogResult> execute(GetAgentAuditLogsQuery query);
}

