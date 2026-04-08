package com.ecommerce.identity.application.port.in.agent;

import com.ecommerce.identity.application.dto.agent.result.AgentIdentityResult;
import java.util.List;

public interface ListMyAgentIdentitiesQueryPort {
    List<AgentIdentityResult> execute(String userId);
}
