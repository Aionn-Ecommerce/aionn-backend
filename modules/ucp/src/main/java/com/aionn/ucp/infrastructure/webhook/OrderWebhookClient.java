package com.aionn.ucp.infrastructure.webhook;

import com.aionn.ucp.infrastructure.config.UcpProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Slf4j
@Component
public class OrderWebhookClient {

    private final RestClient restClient;

    public OrderWebhookClient(UcpProperties properties) {
        Duration timeout = Duration.ofMillis(properties.getWebhook().getRequestTimeoutMs());
        this.restClient = RestClient.builder()
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {
                    {
                        setConnectTimeout((int) timeout.toMillis());
                        setReadTimeout((int) timeout.toMillis());
                    }
                })
                .build();
    }

    /** @return HTTP status code of the response. Throws on transport failure. */
    public int post(String url, String payloadJson) {
        try {
            ResponseEntity<Void> response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payloadJson)
                    .retrieve()
                    .toBodilessEntity();
            return response.getStatusCode().value();
        } catch (RestClientException ex) {
            log.warn("Webhook POST failed to {}: {}", url, ex.getMessage());
            throw ex;
        }
    }
}
