package com.aionn.ucp.application.service;

import com.aionn.ucp.application.dto.envelope.CapabilityRef;
import com.aionn.ucp.application.dto.envelope.UcpEnvelope;
import com.aionn.ucp.infrastructure.config.UcpProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UcpEnvelopeFactory {

    private final UcpProperties properties;
    private final @org.springframework.context.annotation.Lazy CapabilityNegotiationService negotiationService;

    public UcpEnvelope ok(String... capabilityNames) {
        return UcpEnvelope.ok(properties.getProtocolVersion(), capabilities(capabilityNames));
    }

    public UcpEnvelope error(String... capabilityNames) {
        return UcpEnvelope.error(properties.getProtocolVersion(), capabilities(capabilityNames));
    }

    private Map<String, List<CapabilityRef>> capabilities(String... names) {
        if (names == null || names.length == 0) {
            return null;
        }

        Set<String> negotiated = null;
        String profileUrl = getPlatformProfileUrl();
        if (profileUrl != null) {
            try {
                negotiated = negotiationService.negotiate(profileUrl);
            } catch (Exception e) {
                // fallback to no-negotiation
            }
        }

        Map<String, List<CapabilityRef>> caps = new LinkedHashMap<>();
        for (String name : names) {
            if (negotiated == null || negotiated.contains(name)) {
                caps.put(name, List.of(new CapabilityRef(properties.getProtocolVersion())));
            }
        }
        return caps;
    }

    private String getPlatformProfileUrl() {
        try {
            var attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return (String) attributes.getAttribute(
                        com.aionn.ucp.infrastructure.web.UcpAgentFilter.PLATFORM_PROFILE_URL_ATTR,
                        org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST);
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}
