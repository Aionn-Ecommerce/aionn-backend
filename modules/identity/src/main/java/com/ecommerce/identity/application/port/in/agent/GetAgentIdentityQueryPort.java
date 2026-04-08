package com.ecommerce.identity.application.port.in.agent;

import com.ecommerce.identity.application.dto.agent.query.GetAgentIdentityQuery;
import com.ecommerce.identity.application.dto.agent.result.AgentIdentityResult;

public interface GetAgentIdentityQueryPort {
    AgentIdentityResult execute(GetAgentIdentityQuery query);
}
