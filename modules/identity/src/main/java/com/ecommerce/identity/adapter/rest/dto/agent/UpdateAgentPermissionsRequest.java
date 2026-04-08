package com.ecommerce.identity.adapter.rest.dto.agent;

import jakarta.validation.constraints.NotBlank;

public record UpdateAgentPermissionsRequest(
        @NotBlank(message = "Permissions JSON is required")
        String permissionsJson) {
}


