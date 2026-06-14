package com.aionn.ucp.application.dto.envelope;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UcpEnvelope(
        String version,
        String status,
        Map<String, List<CapabilityRef>> capabilities) {

    public static UcpEnvelope ok(String version, Map<String, List<CapabilityRef>> capabilities) {
        return new UcpEnvelope(version, null, capabilities);
    }

    public static UcpEnvelope error(String version, Map<String, List<CapabilityRef>> capabilities) {
        return new UcpEnvelope(version, "error", capabilities);
    }
}
