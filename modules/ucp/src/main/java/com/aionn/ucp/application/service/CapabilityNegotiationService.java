package com.aionn.ucp.application.service;

import com.aionn.ucp.infrastructure.gateway.PlatformProfileFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CapabilityNegotiationService {

    private final PlatformProfileFetcher profileFetcher;
    private final BusinessProfileService businessProfileService;

    @SuppressWarnings("unchecked")
    public Set<String> negotiate(String platformProfileUrl) {
        if (platformProfileUrl == null || platformProfileUrl.isBlank()) {
            return getAllBusinessCapabilities();
        }

        Map<String, Object> platformProfile = profileFetcher.fetch(platformProfileUrl);
        if (platformProfile == null) {
            log.warn("Could not fetch platform profile, returning all business capabilities");
            return getAllBusinessCapabilities();
        }

        Set<String> platformCapabilities = new LinkedHashSet<>();
        try {
            Map<String, Object> ucp = (Map<String, Object>) platformProfile.get("ucp");
            if (ucp != null && ucp.containsKey("capabilities")) {
                Map<String, Object> caps = (Map<String, Object>) ucp.get("capabilities");
                platformCapabilities.addAll(caps.keySet());
            }
        } catch (ClassCastException ex) {
            log.warn("Malformed platform profile capabilities: {}", ex.getMessage());
            return getAllBusinessCapabilities();
        }

        Set<String> businessCapabilities = getAllBusinessCapabilities();
        Set<String> intersection = new LinkedHashSet<>();
        for (String cap : businessCapabilities) {
            if (platformCapabilities.contains(cap)) {
                intersection.add(cap);
            }
        }

        pruneOrphanedExtensions(intersection);

        log.info("Capability negotiation: business={} platform={} result={}",
                businessCapabilities, platformCapabilities, intersection);

        return intersection.isEmpty() ? businessCapabilities : intersection;
    }

    private Set<String> getAllBusinessCapabilities() {
        Set<String> caps = new LinkedHashSet<>();
        var profile = businessProfileService.buildProfile();
        if (profile.ucp() != null && profile.ucp().capabilities() != null) {
            caps.addAll(profile.ucp().capabilities().keySet());
        }
        return caps;
    }

    private void pruneOrphanedExtensions(Set<String> capabilities) {
        Map<String, String> extensionParents = Map.of(
                "dev.ucp.shopping.fulfillment", "dev.ucp.shopping.checkout",
                "dev.ucp.shopping.discount", "dev.ucp.shopping.checkout");

        Set<String> toRemove = new HashSet<>();
        for (var entry : extensionParents.entrySet()) {
            if (capabilities.contains(entry.getKey()) && !capabilities.contains(entry.getValue())) {
                toRemove.add(entry.getKey());
                log.info("Pruning orphaned extension {} (parent {} not negotiated)",
                        entry.getKey(), entry.getValue());
            }
        }
        capabilities.removeAll(toRemove);
    }
}
