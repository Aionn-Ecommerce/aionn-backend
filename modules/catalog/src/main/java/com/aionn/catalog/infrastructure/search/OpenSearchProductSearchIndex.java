package com.aionn.catalog.infrastructure.search;

import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.application.port.out.ProductSearchIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * OpenSearch-backed search index. The actual cluster wiring (host, auth,
 * index name) is delegated to {@link OpenSearchConfig}. We do not declare
 * the {@code @Component} unless {@code catalog.search.provider=opensearch} so
 * dev environments can keep using the in-process variant without pulling
 * the network dependency.
 *
 * <p>
 * Implementation note: heavy lifting (mappings, retry, dead letter) is left
 * to the next milestone. The contract is in place so the application layer
 * does not change when we switch.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "catalog.search", name = "provider", havingValue = "opensearch")
public class OpenSearchProductSearchIndex implements ProductSearchIndex {

    private static final String INDEX_NAME = "catalog-products";

    private final OpenSearchClient client;

    @Override
    public void index(ProductSearchDocument document) {
        try {
            client.index(IndexRequest.of(req -> req
                    .index(INDEX_NAME)
                    .id(document.productId())
                    .document(document)));
        } catch (IOException ex) {
            log.error("OpenSearch index failed for product {}", document.productId(), ex);
            throw new IllegalStateException("OpenSearch index failed", ex);
        }
    }

    @Override
    public void indexAll(List<ProductSearchDocument> documents) {
        if (documents.isEmpty()) {
            return;
        }
        try {
            BulkRequest.Builder bulk = new BulkRequest.Builder();
            for (ProductSearchDocument doc : documents) {
                bulk.operations(op -> op.index(idx -> idx
                        .index(INDEX_NAME)
                        .id(doc.productId())
                        .document(doc)));
            }
            client.bulk(bulk.build());
        } catch (IOException ex) {
            log.error("OpenSearch bulk index failed", ex);
            throw new IllegalStateException("OpenSearch bulk index failed", ex);
        }
    }

    @Override
    public void remove(String productId) {
        try {
            client.delete(DeleteRequest.of(req -> req.index(INDEX_NAME).id(productId)));
        } catch (IOException ex) {
            log.error("OpenSearch delete failed for product {}", productId, ex);
            throw new IllegalStateException("OpenSearch delete failed", ex);
        }
    }

    @Override
    public void removeAll(List<String> productIds) {
        productIds.forEach(this::remove);
    }
}

