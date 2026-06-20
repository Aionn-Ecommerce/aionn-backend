package com.aionn.catalog.infrastructure.search;

import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.application.port.out.ProductSearchIndex;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ResilientProductSearchIndexTest {

    private ProductSearchIndex delegate;
    private SimpleMeterRegistry meterRegistry;
    private RetryRegistry retryRegistry;
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        delegate = mock(ProductSearchIndex.class);
        meterRegistry = new SimpleMeterRegistry();
        retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(java.time.Duration.ofMillis(1))
                .build());
        circuitBreakerRegistry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .failureRateThreshold(99.0f)
                .minimumNumberOfCalls(20)
                .build());
        // Register the named instances upfront so the resilient index picks them up.
        retryRegistry.retry("catalog-search-index");
        circuitBreakerRegistry.circuitBreaker("catalog-search-index");
    }

    private ResilientProductSearchIndex newSubject() {
        return new ResilientProductSearchIndex(
                List.of(delegate), retryRegistry, circuitBreakerRegistry, meterRegistry);
    }

    private ProductSearchDocument doc(String id) {
        return new ProductSearchDocument(
                id, "m-1", "Name", null, null, List.of(), List.of(), List.of(), List.of(),
                java.util.Map.of(), null, null, "VND", "PUBLISHED", Instant.now(),
                0.0, false, List.of(), null, null, 0L, false, null, null);
    }

    @Test
    void indexDelegatesToUnderlyingPort() {
        ResilientProductSearchIndex subject = newSubject();
        ProductSearchDocument document = doc("p-1");

        subject.index(document);

        verify(delegate).index(document);
        assertThat(meterRegistry.counter("catalog.search.index", "outcome", "success").count())
                .isEqualTo(1.0);
    }

    @Test
    void indexSwallowsExceptionsAndIncrementsFailureCounter() {
        doThrow(new RuntimeException("opensearch down")).when(delegate).index(org.mockito.ArgumentMatchers.any());
        ResilientProductSearchIndex subject = newSubject();

        subject.index(doc("p-1"));

        assertThat(meterRegistry.counter("catalog.search.index", "outcome", "failure").count())
                .isEqualTo(1.0);
        verify(delegate, atLeastOnce()).index(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void indexAllSkipsEmptyList() {
        ResilientProductSearchIndex subject = newSubject();

        subject.indexAll(List.of());

        verify(delegate, never()).indexAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void removeDelegatesToUnderlyingPort() {
        ResilientProductSearchIndex subject = newSubject();
        doNothing().when(delegate).remove("p-1");

        subject.remove("p-1");

        verify(delegate).remove("p-1");
    }

    @Test
    void removeAllSkipsEmptyList() {
        ResilientProductSearchIndex subject = newSubject();

        subject.removeAll(List.of());
        subject.removeAll(null);

        verify(delegate, never()).removeAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void constructorRejectsWhenNoUnderlyingPortAvailable() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> new ResilientProductSearchIndex(
                        List.of(), retryRegistry, circuitBreakerRegistry, meterRegistry));
    }
}
