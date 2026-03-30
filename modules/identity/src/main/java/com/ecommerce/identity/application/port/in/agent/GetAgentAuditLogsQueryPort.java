package com.ecommerce.identity.application.port.in.agent;

import com.ecommerce.identity.application.dto.agent.GetAgentAuditLogsQuery;
import com.ecommerce.identity.application.dto.agent.AgentAuditLogResult;
import java.util.List;

public interface GetAgentAuditLogsQueryPort {
    List<AgentAuditLogResult> execute(GetAgentAuditLogsQuery query);
}