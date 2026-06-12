package com.aionn.catalog.infrastructure.search;

import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.application.port.out.ProductSearchIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
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
            BulkResponse response = client.bulk(bulk.build());
            if (response.errors()) {
                response.items().forEach(item -> {
                    if (item.error() != null) {
                        log.error("OpenSearch bulk index failed for id={} status={} reason={}",
                                item.id(), item.status(), item.error().reason());
                    }
                });
            }
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
