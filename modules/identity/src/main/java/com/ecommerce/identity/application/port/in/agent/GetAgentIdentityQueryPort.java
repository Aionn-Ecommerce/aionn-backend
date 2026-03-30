package com.ecommerce.identity.application.port.in.agent;

import com.ecommerce.identity.application.dto.agent.GetAgentIdentityQuery;
import com.ecommerce.identity.application.dto.agent.AgentIdentityResult;

public interface GetAgentIdentityQueryPort {
    AgentIdentityResult execute(GetAgentIdentityQuery query);
}