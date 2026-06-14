package com.aionn.ucp.infrastructure.gateway;

import com.aionn.ucp.infrastructure.config.UcpProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformProfileFetcher {

    private final UcpProperties properties;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, CachedProfile> cache = new ConcurrentHashMap<>();

    /**
     * Fetches the platform profile from the given URL.
     * Returns null if fetch fails or URL is invalid.
     */
    public Map<String, Object> fetch(String profileUrl) {
        if (profileUrl == null || profileUrl.isBlank()) {
            return null;
        }

        // Check cache
        CachedProfile cached = cache.get(profileUrl);
        long ttlMs = properties.getPlatformProfile().getCacheTtlSeconds() * 1000L;
        if (cached != null && System.currentTimeMillis() - cached.fetchedAt < ttlMs) {
            return cached.profile;
        }

        // Fetch fresh
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(properties.getPlatformProfile().getFetchTimeoutMs()))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(profileUrl))
                    .timeout(Duration.ofMillis(properties.getPlatformProfile().getFetchTimeoutMs()))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Map<String, Object> profile = objectMapper.readValue(
                        response.body(), new TypeReference<>() {
                        });
                cache.put(profileUrl, new CachedProfile(profile, System.currentTimeMillis()));
                log.info("Fetched platform profile from {}", profileUrl);
                return profile;
            } else {
                log.warn("Failed to fetch platform profile from {}: HTTP {}", profileUrl, response.statusCode());
                return null;
            }
        } catch (Exception ex) {
            log.warn("Error fetching platform profile from {}: {}", profileUrl, ex.getMessage());
            return null;
        }
    }

    private record CachedProfile(Map<String, Object> profile, long fetchedAt) {
    }
}
