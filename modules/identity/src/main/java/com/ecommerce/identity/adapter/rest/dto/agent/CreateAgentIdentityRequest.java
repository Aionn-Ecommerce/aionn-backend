package com.ecommerce.identity.adapter.rest.dto.agent;

import jakarta.validation.constraints.NotBlank;

public record CreateAgentIdentityRequest(
        @NotBlank(message = "Agent name is required")
        String agentName) {
}


