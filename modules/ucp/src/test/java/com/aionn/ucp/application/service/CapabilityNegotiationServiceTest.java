package com.aionn.ucp.application.service;

import com.aionn.ucp.application.dto.profile.BusinessProfileDto;
import com.aionn.ucp.application.dto.profile.CapabilityDeclaration;
import com.aionn.ucp.domain.model.CapabilityName;
import com.aionn.ucp.infrastructure.config.UcpProperties;
import com.aionn.ucp.infrastructure.gateway.PlatformProfileFetcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapabilityNegotiationServiceTest {

    @Mock
    private PlatformProfileFetcher profileFetcher;

    @Mock
    private BusinessProfileService businessProfileService;

    @Mock
    private UcpProperties properties;

    @InjectMocks
    private CapabilityNegotiationService capabilityNegotiationService;

    @Test
    @DisplayName("negotiate() should return all business capabilities if platform profile URL is null/blank")
    void negotiate_returnsAllBusinessCapabilities_whenUrlIsBlank() {
        BusinessProfileDto businessProfile = mockBusinessProfile(
                CapabilityName.CHECKOUT, CapabilityName.ORDER
        );
        when(businessProfileService.buildProfile()).thenReturn(businessProfile);

        Set<String> result = capabilityNegotiationService.negotiate("");

        assertEquals(2, result.size());
        assertTrue(result.contains(CapabilityName.CHECKOUT));
        assertTrue(result.contains(CapabilityName.ORDER));
        verifyNoInteractions(profileFetcher);
    }

    @Test
    @DisplayName("negotiate() should return intersection and prune orphaned extensions")
    void negotiate_returnsIntersectionAndPrunesOrphans() {
        BusinessProfileDto businessProfile = mockBusinessProfile(
                CapabilityName.CHECKOUT, CapabilityName.FULFILLMENT, CapabilityName.ORDER
        );
        when(businessProfileService.buildProfile()).thenReturn(businessProfile);

        // Platform supports fulfillment (which is checkout extension) but NOT checkout core
        Map<String, Object> platformProfile = new LinkedHashMap<>();
        Map<String, Object> ucp = new LinkedHashMap<>();
        Map<String, Object> caps = new LinkedHashMap<>();
        caps.put(CapabilityName.FULFILLMENT, Map.of());
        caps.put(CapabilityName.ORDER, Map.of());
        ucp.put("capabilities", caps);
        platformProfile.put("ucp", ucp);

        when(profileFetcher.fetch("http://platform/profile.json")).thenReturn(platformProfile);

        Set<String> result = capabilityNegotiationService.negotiate("http://platform/profile.json");

        // FULFILLMENT should be pruned because CHECKOUT is missing
        assertEquals(1, result.size());
        assertTrue(result.contains(CapabilityName.ORDER));
        assertFalse(result.contains(CapabilityName.FULFILLMENT));
    }

    private BusinessProfileDto mockBusinessProfile(String... capabilities) {
        Map<String, List<CapabilityDeclaration>> caps = new LinkedHashMap<>();
        for (String c : capabilities) {
            caps.put(c, List.of(CapabilityDeclaration.of("2026-01-23", "spec", "schema")));
        }
        BusinessProfileDto.UcpSection ucp = new BusinessProfileDto.UcpSection(
                "2026-01-23", Map.of(), caps, Map.of()
        );
        return new BusinessProfileDto(ucp, null);
    }
}
