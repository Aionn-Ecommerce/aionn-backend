package com.aionn.catalog.infrastructure.search;

import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.application.port.out.ProductSearchIndex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-process search index. Keeps documents in a thread-safe map so dev / unit
 * tests can run without a live OpenSearch cluster. Queries can be added later;
 * for now it is enough to cover writes.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "catalog.search", name = "provider", havingValue = "in-process", matchIfMissing = true)
public class InProcessProductSearchIndex implements ProductSearchIndex {

    private final Map<String, ProductSearchDocument> store = new ConcurrentHashMap<>();

    @Override
    public void index(ProductSearchDocument document) {
        store.put(document.productId(), document);
        log.debug("Indexed product {}", document.productId());
    }

    @Override
    public void indexAll(List<ProductSearchDocument> documents) {
        documents.forEach(this::index);
    }

    @Override
    public void remove(String productId) {
        store.remove(productId);
        log.debug("Removed product {} from search index", productId);
    }

    @Override
    public void removeAll(List<String> productIds) {
        productIds.forEach(store::remove);
    }

    /** Test helper. Not part of the {@link ProductSearchIndex} contract. */
    public Optional<ProductSearchDocument> get(String productId) {
        return Optional.ofNullable(store.get(productId));
    }

    /** Test helper. Not part of the {@link ProductSearchIndex} contract. */
    public Collection<ProductSearchDocument> all() {
        return Collections.unmodifiableCollection(store.values());
    }

    /** Test helper used by integration tests; clears the index in-place. */
    public Map<String, ProductSearchDocument> snapshot() {
        return store.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}

