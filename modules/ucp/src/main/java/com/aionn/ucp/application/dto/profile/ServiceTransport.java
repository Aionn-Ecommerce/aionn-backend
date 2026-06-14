package com.aionn.ucp.application.dto.profile;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A transport binding within a service registry, e.g. {transport:"rest",
 * endpoint:"..."}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServiceTransport(
        String version,
        String spec,
        String transport,
        String endpoint,
        String schema) {
}
