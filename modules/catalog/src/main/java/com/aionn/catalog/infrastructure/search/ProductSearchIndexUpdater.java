package com.aionn.catalog.infrastructure.search;

import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.application.mapper.ProductResultMapper;
import com.aionn.catalog.application.port.out.AttributeTemplatePersistencePort;
import com.aionn.catalog.application.port.out.ProductPersistencePort;
import com.aionn.catalog.application.port.out.ProductSearchIndex;
import com.aionn.catalog.domain.event.ProductEvents;
import com.aionn.catalog.domain.model.AttributeTemplate.AttributeDefinition;
import com.aionn.catalog.domain.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Updates the search index after Product domain events are committed.
 *
 * <p>
 * Decouples search-index maintenance from the business transaction so that
 * slow OpenSearch latency or transient outages do not extend DB transactions
 * or hold connection-pool slots. The {@link ResilientProductSearchIndex}
 * wrapper still swallows failures, so a flaky search backend never propagates
 * to the listener.
 * </p>
 *
 * <p>
 * One method per domain event keeps the dispatch flat and easy to extend.
 * Re-indexing on every mutating event ensures the search document stays in
 * sync with the latest committed state, even when multiple events are
 * recorded by the same use case.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSearchIndexUpdater {

    private final ProductPersistencePort productRepository;
    private final ProductSearchIndex searchIndex;
    private final ProductResultMapper productResultMapper;
    private final AttributeTemplatePersistencePort attributeTemplateRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onProductPublished(ProductEvents.ProductPublished event) {
        reindex(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onProductRestored(ProductEvents.ProductRestored event) {
        reindex(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductRejected(ProductEvents.ProductRejected event) {
        searchIndex.remove(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductDeactivated(ProductEvents.ProductDeactivated event) {
        searchIndex.remove(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductEmergencyTakedown(ProductEvents.ProductEmergencyTakedown event) {
        searchIndex.remove(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onProductVariantDefined(ProductEvents.ProductVariantDefined event) {
        reindexIfSearchable(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onProductVariantRemoved(ProductEvents.ProductVariantRemoved event) {
        reindexIfSearchable(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onProductVariantPriceChanged(ProductEvents.ProductVariantPriceChanged event) {
        reindexIfSearchable(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onProductMediaUpdated(ProductEvents.ProductMediaUpdated event) {
        reindexIfSearchable(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onProductBrandAssigned(ProductEvents.ProductBrandAssigned event) {
        reindexIfSearchable(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onProductCategorized(ProductEvents.ProductCategorized event) {
        reindexIfSearchable(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onProductMetadataUpdated(ProductEvents.ProductMetadataUpdated event) {
        reindexIfSearchable(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onProductCollectionAssigned(ProductEvents.ProductCollectionAssigned event) {
        reindexIfSearchable(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onProductAttributesDefined(ProductEvents.ProductAttributesDefined event) {
        reindexIfSearchable(event.productId());
    }

    private void reindex(String productId) {
        productRepository.findById(productId).ifPresent(product -> searchIndex.index(buildSearchDocument(product)));
    }

    private void reindexIfSearchable(String productId) {
        productRepository.findById(productId).ifPresent(product -> {
            if (product.getStatus().isSearchable()) {
                searchIndex.index(buildSearchDocument(product));
            }
        });
    }

    private ProductSearchDocument buildSearchDocument(Product product) {
        Map<String, String> filterable = new LinkedHashMap<>();
        if (!product.attributes().isEmpty()) {
            for (String categoryId : product.categoryIds()) {
                attributeTemplateRepository.findByCategoryId(categoryId).ifPresent(template -> {
                    for (Map.Entry<String, AttributeDefinition> def : template.snapshot().entrySet()) {
                        if (def.getValue().filterable()) {
                            String value = product.attributes().get(def.getKey());
                            if (value != null) {
                                filterable.put(def.getKey(), value);
                            }
                        }
                    }
                });
            }
        }
        return productResultMapper.toSearchDocument(product, filterable);
    }
}
