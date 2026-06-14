package com.aionn.ucp.application.dto.profile;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Capability advertisement entry inside `ucp.capabilities[name][]`. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CapabilityDeclaration(
        String version,
        String spec,
        String schema,
        Object extends_,
        Object config) {

    public static CapabilityDeclaration of(String version, String spec, String schema) {
        return new CapabilityDeclaration(version, spec, schema, null, null);
    }
}
