package com.aionn.catalog.application.port.out;

import com.aionn.catalog.application.dto.search.ProductSearchDocument;

import java.util.List;

/**
 * Output port for AI/search index updates. Two implementations are provided:
 * an in-memory {@code InProcessProductSearchIndex} (assume-correct, dev/test)
 * and {@code OpenSearchProductSearchIndex} (real cluster). The application
 * layer never knows which one is wired.
 */
public interface ProductSearchIndex {

    /** Upsert a product document. Called when a product is published / updated. */
    void index(ProductSearchDocument document);

    /** Bulk upsert (used by collection assign / bulk price). */
    void indexAll(List<ProductSearchDocument> documents);

    /** Remove from the search index (hidden / takedown / variant deletion). */
    void remove(String productId);

    /** Remove a list of product ids in one round trip. */
    void removeAll(List<String> productIds);
}

