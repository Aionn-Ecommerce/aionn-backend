package com.aionn.ucp.application.dto.profile;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Business profile served at `/.well-known/ucp`. Contains protocol metadata
 * plus the service/capability registry advertising what this business supports.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BusinessProfileDto(
        UcpSection ucp,
        List<SigningKey> signing_keys) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record UcpSection(
            String version,
            Map<String, List<ServiceTransport>> services,
            Map<String, List<CapabilityDeclaration>> capabilities,
            Map<String, List<Object>> payment_handlers) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SigningKey(
            String kid,
            String kty,
            String crv,
            String x,
            String y,
            String use,
            String alg) {
    }
}
