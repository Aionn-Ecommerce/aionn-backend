package com.aionn.ucp.application.service;

import com.aionn.ucp.application.dto.profile.BusinessProfileDto;
import com.aionn.ucp.domain.model.CapabilityName;
import com.aionn.ucp.infrastructure.config.UcpProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessProfileServiceTest {

    @Mock
    private UcpProperties properties;

    @InjectMocks
    private BusinessProfileService businessProfileService;

    private UcpProperties.Capabilities capabilities;
    private UcpProperties.Signature signature;

    @BeforeEach
    void setUp() {
        capabilities = new UcpProperties.Capabilities();
        signature = new UcpProperties.Signature();
        
        when(properties.getProtocolVersion()).thenReturn("2026-01-23");
        when(properties.getSchemaBaseUrl()).thenReturn("https://ucp.dev/2026-01-23");
        when(properties.getSpecBaseUrl()).thenReturn("https://ucp.dev/2026-01-23/specification");
        when(properties.getEndpointBaseUrl()).thenReturn("http://localhost:8080/");
        
        when(properties.getCapabilities()).thenReturn(capabilities);
        when(properties.getSignature()).thenReturn(signature);
    }

    @Test
    @DisplayName("buildProfile() should return all capabilities when all are enabled")
    void buildProfile_returnsAllCapabilities() {
        capabilities.setCatalogSearch(true);
        capabilities.setCatalogLookup(true);
        capabilities.setCart(true);
        capabilities.setCheckout(true);
        capabilities.setOrder(true);
        capabilities.setFulfillment(true);
        capabilities.setDiscount(true);
        capabilities.setIdentityLinking(true);

        BusinessProfileDto profile = businessProfileService.buildProfile();

        assertNotNull(profile);
        assertNotNull(profile.ucp());
        assertEquals("2026-01-23", profile.ucp().version());

        var caps = profile.ucp().capabilities();
        assertTrue(caps.containsKey(CapabilityName.CATALOG_SEARCH));
        assertTrue(caps.containsKey(CapabilityName.CATALOG_LOOKUP));
        assertTrue(caps.containsKey(CapabilityName.CART));
        assertTrue(caps.containsKey(CapabilityName.CHECKOUT));
        assertTrue(caps.containsKey(CapabilityName.ORDER));
        assertTrue(caps.containsKey(CapabilityName.FULFILLMENT));
        assertTrue(caps.containsKey(CapabilityName.DISCOUNT));
        assertTrue(caps.containsKey(CapabilityName.IDENTITY_LINKING));
    }

    @Test
    @DisplayName("buildProfile() should exclude disabled capabilities")
    void buildProfile_excludesDisabledCapabilities() {
        capabilities.setCatalogSearch(false);
        capabilities.setCatalogLookup(true);
        capabilities.setCart(false);
        capabilities.setCheckout(true);
        capabilities.setOrder(false);
        capabilities.setFulfillment(false);
        capabilities.setDiscount(false);
        capabilities.setIdentityLinking(false);

        BusinessProfileDto profile = businessProfileService.buildProfile();

        assertNotNull(profile);
        var caps = profile.ucp().capabilities();
        assertFalse(caps.containsKey(CapabilityName.CATALOG_SEARCH));
        assertTrue(caps.containsKey(CapabilityName.CATALOG_LOOKUP));
        assertFalse(caps.containsKey(CapabilityName.CART));
        assertTrue(caps.containsKey(CapabilityName.CHECKOUT));
        assertFalse(caps.containsKey(CapabilityName.ORDER));
        assertFalse(caps.containsKey(CapabilityName.FULFILLMENT));
        assertFalse(caps.containsKey(CapabilityName.DISCOUNT));
        assertFalse(caps.containsKey(CapabilityName.IDENTITY_LINKING));
    }
}
