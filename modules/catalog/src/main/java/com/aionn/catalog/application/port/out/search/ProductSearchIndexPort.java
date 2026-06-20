package com.aionn.catalog.application.port.out.search;

import com.aionn.catalog.application.dto.search.ProductSearchDocument;

import java.util.List;

public interface ProductSearchIndexPort {

    void index(ProductSearchDocument document);

    void indexAll(List<ProductSearchDocument> documents);

    void remove(String productId);

    void removeAll(List<String> productIds);
}
