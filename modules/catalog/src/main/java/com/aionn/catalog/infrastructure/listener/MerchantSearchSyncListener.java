package com.aionn.catalog.infrastructure.listener;

import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.application.mapper.ProductResultMapper;
import com.aionn.catalog.application.port.out.ProductPersistencePort;
import com.aionn.catalog.application.port.out.ProductSearchIndex;
import com.aionn.catalog.domain.event.MerchantEvents;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.valueobject.ProductStatus;
import com.aionn.catalog.infrastructure.config.CatalogProperties;
import com.aionn.sharedkernel.domain.vo.OffsetPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantSearchSyncListener {

    private final ProductPersistencePort productRepository;
    private final ProductSearchIndex searchIndex;
    private final ProductResultMapper productResultMapper;
    private final CatalogProperties catalogProperties;

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
                    .map(p -> productResultMapper.toSearchDocument(p, Map.of()))
                    .toList();
            searchIndex.indexAll(docs);
        });
    }

    private void forEachPage(String merchantId, Consumer<List<Product>> handler) {
        int pageSize = catalogProperties.merchantSearchSync().pageSize();
        int page = 0;
        while (true) {
            List<Product> batch = productRepository.findByMerchant(merchantId,
                    OffsetPagination.of(page, pageSize));
            if (batch.isEmpty()) {
                return;
            }
            handler.accept(batch);
            if (batch.size() < pageSize) {
                return;
            }
            page++;
        }
    }
}
