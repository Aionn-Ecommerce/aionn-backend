package com.aionn.ucp.application.service;

import com.aionn.ucp.domain.model.UcpCapability;
import com.aionn.ucp.domain.model.UcpEnvelope;
import com.aionn.ucp.domain.model.UcpMetadata;
import com.aionn.ucp.domain.model.UcpStatus;
import com.aionn.ucp.infrastructure.config.properties.UcpCapabilityProperties;
import com.aionn.ucp.infrastructure.config.properties.UcpProtocolProperties;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UcpEnvelopeServiceTest {

    private static final String VERSION = "2025-01-01";
    private static final String SCHEMA_BASE = "https://ucp.dev/schemas";

    private final UcpProtocolProperties protocolProps = new UcpProtocolProperties(VERSION, SCHEMA_BASE);

    @Test
    void buildSuccessMetadataIncludesAllEnabledCapabilitiesUnderReverseDomainKeys() {
        UcpCapabilityProperties caps = new UcpCapabilityProperties(
                new UcpCapabilityProperties.Capability(true, VERSION),
                new UcpCapabilityProperties.Capability(true, VERSION),
                new UcpCapabilityProperties.Capability(true, VERSION),
                new UcpCapabilityProperties.Capability(true, VERSION));

        UcpEnvelopeService svc = new UcpEnvelopeService(protocolProps, caps);
        UcpMetadata md = svc.buildSuccessMetadata();

        assertThat(md.version()).isEqualTo(VERSION);
        assertThat(md.status()).isEqualTo(UcpStatus.SUCCESS.wireValue());
        assertThat(md.capabilities()).containsKeys(
                "shopping.cart", "shopping.checkout", "shopping.order", "shopping.identity_linking");

        UcpCapability cart = md.capabilities().get("shopping.cart").get(0);
        assertThat(cart.name()).isEqualTo("shopping.cart");
        assertThat(cart.version()).isEqualTo(VERSION);
        assertThat(cart.spec()).isEqualTo(SCHEMA_BASE + "/specification/cart");
        assertThat(cart.schema()).isEqualTo(SCHEMA_BASE + "/cart.json");
    }

    @Test
    void buildSuccessMetadataExcludesDisabledCapabilities() {
        UcpCapabilityProperties caps = new UcpCapabilityProperties(
                new UcpCapabilityProperties.Capability(true, VERSION),
                new UcpCapabilityProperties.Capability(false, VERSION),
                new UcpCapabilityProperties.Capability(false, VERSION),
                new UcpCapabilityProperties.Capability(false, VERSION));

        UcpEnvelopeService svc = new UcpEnvelopeService(protocolProps, caps);
        UcpMetadata md = svc.buildSuccessMetadata();

        assertThat(md.capabilities()).containsKey("shopping.cart");
        assertThat(md.capabilities()).doesNotContainKeys(
                "shopping.checkout", "shopping.order", "shopping.identity_linking");
    }

    @Test
    void buildSuccessMetadataHandlesAllDisabled() {
        UcpCapabilityProperties caps = new UcpCapabilityProperties(
                new UcpCapabilityProperties.Capability(false, VERSION),
                new UcpCapabilityProperties.Capability(false, VERSION),
                new UcpCapabilityProperties.Capability(false, VERSION),
                new UcpCapabilityProperties.Capability(false, VERSION));

        UcpMetadata md = new UcpEnvelopeService(protocolProps, caps).buildSuccessMetadata();

        assertThat(md.capabilities()).isEmpty();
        assertThat(md.status()).isEqualTo("success");
    }

    @Test
    void wrapProducesEnvelopeWithDataAndMetadata() {
        UcpCapabilityProperties caps = new UcpCapabilityProperties(
                new UcpCapabilityProperties.Capability(true, VERSION),
                new UcpCapabilityProperties.Capability(false, VERSION),
                new UcpCapabilityProperties.Capability(false, VERSION),
                new UcpCapabilityProperties.Capability(false, VERSION));
        UcpEnvelopeService svc = new UcpEnvelopeService(protocolProps, caps);

        Map<String, Object> data = Map.of("orderId", "o-1");
        UcpEnvelope<Map<String, Object>> env = svc.wrap(data);

        assertThat(env.data()).isSameAs(data);
        assertThat(env.ucp().version()).isEqualTo(VERSION);
        assertThat(env.ucp().status()).isEqualTo("success");
    }

    @Test
    void buildSuccessMetadataTreatsNullCapabilityAsDisabled() {
        UcpCapabilityProperties caps = new UcpCapabilityProperties(
                null,
                new UcpCapabilityProperties.Capability(true, VERSION),
                null,
                new UcpCapabilityProperties.Capability(true, VERSION));

        UcpMetadata md = new UcpEnvelopeService(protocolProps, caps).buildSuccessMetadata();

        assertThat(md.capabilities()).containsOnlyKeys("shopping.checkout", "shopping.identity_linking");

        // Insertion order is preserved.
        List<String> keys = List.copyOf(md.capabilities().keySet());
        assertThat(keys).containsExactly("shopping.checkout", "shopping.identity_linking");
    }
}
