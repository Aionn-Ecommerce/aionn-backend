package com.aionn.catalog.infrastructure.search;

import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenSearch client wiring. Activated only when
 * {@code catalog.search.provider=opensearch}; in dev we run with the
 * in-process index and skip pulling the network dependency.
 */
@Configuration
@ConditionalOnProperty(prefix = "catalog.search", name = "provider", havingValue = "opensearch")
public class OpenSearchConfig {

    @Value("${catalog.search.opensearch.host:localhost}")
    private String host;

    @Value("${catalog.search.opensearch.port:9200}")
    private int port;

    @Value("${catalog.search.opensearch.scheme:http}")
    private String scheme;

    @Bean
    public OpenSearchClient openSearchClient() {
        OpenSearchTransport transport = ApacheHttpClient5TransportBuilder
                .builder(new HttpHost(scheme, host, port))
                .build();
        return new OpenSearchClient(transport);
    }
}

