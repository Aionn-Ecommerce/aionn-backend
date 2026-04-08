package com.ecommerce.identity.application.dto.agent.query;

import com.ecommerce.sharedkernel.application.query.Query;

public record GetAgentIdentityQuery(String userId, String agentId) implements Query {
}
