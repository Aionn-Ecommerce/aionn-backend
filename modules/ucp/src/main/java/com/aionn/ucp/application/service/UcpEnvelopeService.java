package com.aionn.ucp.application.service;

import com.aionn.ucp.domain.model.UcpCapability;
import com.aionn.ucp.domain.model.UcpEnvelope;
import com.aionn.ucp.domain.model.UcpMetadata;
import com.aionn.ucp.domain.model.UcpStatus;
import com.aionn.ucp.infrastructure.config.properties.UcpCapabilityProperties;
import com.aionn.ucp.infrastructure.config.properties.UcpProtocolProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UcpEnvelopeService {

    private final UcpProtocolProperties protocolProperties;
    private final UcpCapabilityProperties capabilityProperties;

    public UcpMetadata buildSuccessMetadata() {
        Map<String, List<UcpCapability>> caps = new LinkedHashMap<>();
        addIfEnabled(caps, "cart", capabilityProperties.cart(), "shopping.cart");
        addIfEnabled(caps, "checkout", capabilityProperties.checkout(), "shopping.checkout");
        addIfEnabled(caps, "order", capabilityProperties.order(), "shopping.order");
        addIfEnabled(caps, "identity_linking", capabilityProperties.identityLinking(), "shopping.identity_linking");
        return new UcpMetadata(
                protocolProperties.version(),
                UcpStatus.SUCCESS.wireValue(),
                caps);
    }

    public <T> UcpEnvelope<T> wrap(T data) {
        return UcpEnvelope.success(buildSuccessMetadata(), data);
    }

    private void addIfEnabled(Map<String, List<UcpCapability>> caps,
            String key,
            UcpCapabilityProperties.Capability cfg,
            String reverseDomainName) {
        if (cfg != null && cfg.enabled()) {
            caps.put(reverseDomainName, List.of(new UcpCapability(
                    reverseDomainName,
                    cfg.version(),
                    protocolProperties.schemaBaseUrl() + "/specification/" + key,
                    protocolProperties.schemaBaseUrl() + "/" + key + ".json")));
        }
    }
}
