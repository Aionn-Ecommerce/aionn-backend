package com.aionn.catalog.infrastructure.listener;

import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.application.mapper.ProductResultMapper;
import com.aionn.catalog.application.port.out.ProductRepository;
import com.aionn.catalog.application.port.out.ProductSearchIndex;
import com.aionn.catalog.domain.event.MerchantEvents;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.valueobject.ProductStatus;
import com.aionn.sharedkernel.domain.vo.OffsetPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Reacts to merchant lifecycle events to keep the search index in sync.
 *
 * <ul>
 * <li>Suspended -> hide every searchable product of the merchant.</li>
 * <li>Activated -> reindex every published product of the merchant.</li>
 * <li>Closed -> remove every product permanently.</li>
 * </ul>
 *
 * <p>
 * We page through products in chunks of 200 to avoid loading the whole
 * catalog of a large merchant into memory.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantSearchSyncListener {

    private static final int PAGE_SIZE = 200;

    private final ProductRepository productRepository;
    private final ProductSearchIndex searchIndex;
    private final ProductResultMapper productResultMapper;

    @EventListener
    public void onSuspended(MerchantEvents.MerchantSuspended event) {
        log.info("Hiding products of suspended merchant {}", event.merchantId());
        forEachPage(event.merchantId(), products -> {
            List<String> productIds = products.stream().map(Product::getProductId).toList();
            searchIndex.removeAll(productIds);
        });
    }

    @EventListener
    public void onClosed(MerchantEvents.MerchantClosed event) {
        log.info("Removing products of closed merchant {}", event.merchantId());
        forEachPage(event.merchantId(), products -> {
            List<String> productIds = products.stream().map(Product::getProductId).toList();
            searchIndex.removeAll(productIds);
        });
    }

    @EventListener
    public void onActivated(MerchantEvents.MerchantActivated event) {
        log.info("Reindexing products of activated merchant {}", event.merchantId());
        forEachPage(event.merchantId(), products -> {
            List<ProductSearchDocument> docs = products.stream()
                    .filter(p -> p.getStatus() == ProductStatus.PUBLISHED)
                    .map(p -> productResultMapper.toSearchDocument(p, java.util.Map.of()))
                    .toList();
            searchIndex.indexAll(docs);
        });
    }

    private void forEachPage(String merchantId, java.util.function.Consumer<List<Product>> handler) {
        int page = 0;
        while (true) {
            List<Product> batch = productRepository.findByMerchant(merchantId,
                    OffsetPagination.of(page, PAGE_SIZE));
            if (batch.isEmpty()) {
                return;
            }
            handler.accept(batch);
            if (batch.size() < PAGE_SIZE) {
                return;
            }
            page++;
        }
    }
}

