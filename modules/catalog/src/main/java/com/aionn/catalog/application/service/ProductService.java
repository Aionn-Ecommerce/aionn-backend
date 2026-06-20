package com.aionn.catalog.application.service;

import com.aionn.catalog.application.dto.product.command.AssignBrandCommand;
import com.aionn.catalog.application.dto.product.command.AssignCategoriesCommand;
import com.aionn.catalog.application.dto.product.command.AssignCollectionsCommand;
import com.aionn.catalog.application.dto.product.command.BulkPriceUpdateCommand;
import com.aionn.catalog.application.dto.product.command.ChangeVariantPriceCommand;
import com.aionn.catalog.application.dto.product.command.CloneCommand;
import com.aionn.catalog.application.dto.product.command.CloneProductCommand;
import com.aionn.catalog.application.dto.product.command.CreateProductCommand;
import com.aionn.catalog.application.dto.product.command.DeactivateCommand;
import com.aionn.catalog.application.dto.product.command.DeactivateProductCommand;
import com.aionn.catalog.application.dto.product.command.DefineAttributesCommand;
import com.aionn.catalog.application.dto.product.command.DefineVariantCommand;
import com.aionn.catalog.application.dto.product.command.EmergencyTakedownCommand;
import com.aionn.catalog.application.dto.product.command.PublishCommand;
import com.aionn.catalog.application.dto.product.command.PublishProductCommand;
import com.aionn.catalog.application.dto.product.command.RejectCommand;
import com.aionn.catalog.application.dto.product.command.RejectProductCommand;
import com.aionn.catalog.application.dto.product.command.RemoveVariantCommand;
import com.aionn.catalog.application.dto.product.command.RestoreCommand;
import com.aionn.catalog.application.dto.product.command.RestoreProductCommand;
import com.aionn.catalog.application.dto.product.command.SubmitForReviewCommand;
import com.aionn.catalog.application.dto.product.command.UpdateAiMetadataCommand;
import com.aionn.catalog.application.dto.product.command.UpdateMediaCommand;
import com.aionn.catalog.application.dto.common.PageResult;
import com.aionn.catalog.application.dto.product.query.SearchProductsQuery;
import com.aionn.catalog.application.dto.product.result.ProductResult;
import com.aionn.catalog.application.dto.search.ProductSearchCriteria;
import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.application.dto.search.ProductSearchResult;
import com.aionn.catalog.application.mapper.ProductResultMapper;
import com.aionn.catalog.application.port.out.AttributeTemplatePersistencePort;
import com.aionn.catalog.application.port.out.BrandPersistencePort;
import com.aionn.catalog.application.port.out.CategoryPersistencePort;
import com.aionn.catalog.application.port.out.MerchantPersistencePort;
import com.aionn.catalog.application.port.out.ProductPersistencePort;
import com.aionn.catalog.application.port.out.UserBrowsingHistoryPersistencePort;
import com.aionn.catalog.domain.model.UserBrowsingHistory;
import com.aionn.catalog.application.port.out.ProductSearchIndex;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.model.AttributeTemplate;
import com.aionn.catalog.domain.model.Brand;
import com.aionn.catalog.application.policy.CatalogValidationConstants;
import com.aionn.catalog.domain.model.Merchant;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.model.ProductVariant;
import com.aionn.catalog.domain.valueobject.BrandStatus;
import com.aionn.catalog.domain.valueobject.ProductStatus;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.aionn.sharedkernel.domain.vo.OffsetPagination;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductPersistencePort productRepository;
    private final UserBrowsingHistoryPersistencePort userBrowsingHistoryRepository;
    private final MerchantPersistencePort merchantRepository;
    private final BrandPersistencePort brandRepository;
    private final CategoryPersistencePort categoryRepository;
    private final AttributeTemplatePersistencePort attributeTemplateRepository;
    private final ProductResultMapper productResultMapper;
    private final ProductSearchIndex searchIndex;
    private final EventPublisher eventPublisher;

    public ProductResult create(CreateProductCommand command) {
        String merchantId = requireMerchantIdForOwner(command.ownerId());
        Product product = Product.create(IdGenerator.ulid(), merchantId, command.name());
        Product saved = productRepository.save(product);
        publish(product);
        return productResultMapper.toResult(saved);
    }

    public ProductResult clone(CloneCommand command) {
        String merchantId = requireMerchantIdForOwner(command.ownerId());
        Product source = required(command.sourceId());
        source.ensureOwnedBy(merchantId);
        Product cloned = Product.create(IdGenerator.ulid(), merchantId, source.getName() + " (Copy)");
        if (source.getBrandId() != null) {
            cloned.assignBrand(source.getBrandId());
        }
        if (!source.categoryIds().isEmpty()) {
            cloned.categorize(source.categoryIds());
        }
        if (!source.tags().isEmpty() || source.getAiDescription() != null) {
            cloned.updateAiMetadata(source.tags(), source.getAiDescription());
        }
        if (!source.attributes().isEmpty()) {
            cloned.defineAttributes(source.attributes());
        }
        Product saved = productRepository.save(cloned);
        publish(cloned);
        return productResultMapper.toResult(saved);
    }

    public ProductResult clone(CloneProductCommand command) {
        return clone(new CloneCommand(command.sourceId(), command.merchantId()));
    }

    public ProductResult defineVariant(DefineVariantCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        Money price = command.price() == null
                ? null
                : Money.of(command.price(), command.currency() == null ? "VND" : command.currency());
        product.defineVariant(IdGenerator.ulid(), command.attributeValues(), price);
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    public ProductResult removeVariant(RemoveVariantCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        product.removeVariant(command.skuId());
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    public ProductResult updateMedia(UpdateMediaCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        product.updateMedia(command.imageList());
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    public ProductResult assignBrand(AssignBrandCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        Brand brand = brandRepository.findById(command.brandId())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.BRAND_NOT_FOUND));
        if (brand.getStatus() != BrandStatus.ACTIVE) {
            throw new CatalogException(CatalogErrorCode.PRODUCT_BRAND_NOT_APPROVED);
        }
        product.assignBrand(command.brandId());
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    public ProductResult categorize(AssignCategoriesCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        java.util.List<com.aionn.catalog.domain.model.Category> found =
                categoryRepository.findAllByIds(command.categoryIds());
        if (found.size() != command.categoryIds().size()) {
            java.util.Set<String> foundIds = found.stream()
                    .map(com.aionn.catalog.domain.model.Category::getCategoryId)
                    .collect(java.util.stream.Collectors.toSet());
            String missing = command.categoryIds().stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElse("?");
            throw new CatalogException(CatalogErrorCode.CATEGORY_NOT_FOUND,
                    "Unknown category: " + missing);
        }
        product.categorize(command.categoryIds());
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    public ProductResult updateAiMetadata(UpdateAiMetadataCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        product.updateAiMetadata(command.tags(), command.aiDescription());
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    public ProductResult assignCollections(AssignCollectionsCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        product.assignToCollections(command.collectionIds());
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    public ProductResult defineAttributes(DefineAttributesCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        if (!product.categoryIds().isEmpty()) {
            java.util.List<AttributeTemplate> templates =
                    attributeTemplateRepository.findByCategoryIds(product.categoryIds());
            for (AttributeTemplate template : templates) {
                for (String key : command.attributes().keySet()) {
                    if (!template.snapshot().containsKey(key)) {
                        throw new CatalogException(CatalogErrorCode.ATTRIBUTE_KEY_NOT_FOUND,
                                "Attribute '" + key + "' is not declared on the category template");
                    }
                }
            }
        }
        product.defineAttributes(command.attributes());
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    public ProductResult publish(PublishCommand command) {
        Product product = required(command.productId());
        product.publish(command.adminId());
        Product saved = productRepository.save(product);
        publish(product);
        searchIndex.index(buildSearchDocument(saved));
        return productResultMapper.toResult(saved);
    }

    public ProductResult publish(PublishProductCommand command) {
        return publish(new PublishCommand(command.productId(), command.adminId()));
    }

    public ProductResult reject(RejectCommand command) {
        Product product = required(command.productId());
        product.reject(command.adminId(), command.reasonCode(), command.feedback());
        Product saved = productRepository.save(product);
        publish(product);
        searchIndex.remove(saved.getProductId());
        return productResultMapper.toResult(saved);
    }

    public ProductResult reject(RejectProductCommand command) {
        return reject(new RejectCommand(
                command.productId(), command.adminId(), command.reasonCode(), command.feedback()));
    }

    public ProductResult deactivate(DeactivateCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        product.deactivate(command.reason());
        Product saved = productRepository.save(product);
        publish(product);
        searchIndex.remove(saved.getProductId());
        return productResultMapper.toResult(saved);
    }

    public ProductResult deactivate(DeactivateProductCommand command) {
        return deactivate(new DeactivateCommand(command.productId(), command.merchantId(), command.reason()));
    }

    public ProductResult restore(RestoreCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        product.restore();
        Product saved = productRepository.save(product);
        publish(product);
        searchIndex.index(buildSearchDocument(saved));
        return productResultMapper.toResult(saved);
    }

    public ProductResult restore(RestoreProductCommand command) {
        return restore(new RestoreCommand(command.productId(), command.merchantId()));
    }

    public ProductResult submitForReview(SubmitForReviewCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        product.submitForReview(command.ownerId());
        Product saved = productRepository.save(product);
        publish(product);
        return productResultMapper.toResult(saved);
    }

    public ProductResult emergencyTakedown(EmergencyTakedownCommand command) {
        Product product = required(command.productId());
        product.emergencyTakedown(command.adminId(), command.reason());
        Product saved = productRepository.save(product);
        publish(product);
        searchIndex.remove(saved.getProductId());
        return productResultMapper.toResult(saved);
    }

    public ProductResult changeVariantPrice(ChangeVariantPriceCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        product.changeVariantPrice(command.skuId(),
                Money.of(command.newPrice(), command.currency() == null ? "VND" : command.currency()));
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    public com.aionn.catalog.application.dto.product.result.BulkPriceUpdateResult bulkPriceUpdate(
            BulkPriceUpdateCommand command) {
        if (command.skuIds() == null || command.skuIds().isEmpty()) {
            throw new CatalogException(CatalogErrorCode.INVALID_ARGUMENT, "skuIds must not be empty");
        }
        if (command.skuIds().size() > CatalogValidationConstants.BULK_PRICE_UPDATE_MAX_SIZE) {
            throw new CatalogException(CatalogErrorCode.PRODUCT_BULK_TOO_LARGE,
                    "Bulk size " + command.skuIds().size() + " exceeds max "
                            + CatalogValidationConstants.BULK_PRICE_UPDATE_MAX_SIZE);
        }

        String merchantId = requireMerchantIdForOwner(command.ownerId());
        List<Product> affected = productRepository.findByMerchantAndSkuIds(merchantId, command.skuIds());
        if (affected.isEmpty()) {
            log.warn("Bulk price update by owner={} matched 0 products for {} skuIds (none owned by merchant {})",
                    command.ownerId(), command.skuIds().size(), merchantId);
            return new com.aionn.catalog.application.dto.product.result.BulkPriceUpdateResult(List.of(), 0);
        }
        java.util.Set<String> affectedProductIds = new LinkedHashSet<>();
        int affectedSkuCount = 0;
        for (Product product : affected) {
            for (ProductVariant variant : product.variants()) {
                if (!command.skuIds().contains(variant.skuId())) {
                    continue;
                }
                affectedSkuCount++;
                BigDecimal oldAmount = variant.price() == null ? BigDecimal.ZERO : variant.price().amount();
                BigDecimal newAmount = applyChange(oldAmount, command);
                String currency = command.currency() != null
                        ? command.currency()
                        : (variant.price() != null ? variant.price().currency() : "VND");
                product.changeVariantPrice(variant.skuId(), Money.of(newAmount, currency));
            }
            productRepository.save(product);
            publish(product);
            reindexIfSearchable(product);
            affectedProductIds.add(product.getProductId());
        }
        return new com.aionn.catalog.application.dto.product.result.BulkPriceUpdateResult(
                List.copyOf(affectedProductIds), affectedSkuCount);
    }

    @Transactional(readOnly = true)
    public ProductResult get(String productId) {
        return productResultMapper.toResult(required(productId));
    }

    @Transactional(readOnly = true)
    public List<ProductResult> getBySkuIds(List<String> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        return productRepository.findBySkuIds(skuIds).stream()
                .map(productResultMapper::toResult)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResult<ProductResult> listByMerchant(String merchantId, int page, int size) {
        var pagination = OffsetPagination.safe(page, size);
        List<Product> products = productRepository.findByMerchant(merchantId, pagination);
        List<ProductResult> results = products.stream()
                .map(productResultMapper::toResult)
                .toList();
        return new PageResult<>(results, page, size, results.size());
    }

    @Transactional(readOnly = true)
    public PageResult<ProductResult> listByMerchant(String merchantId, OffsetPagination pagination) {
        return listByMerchant(merchantId, pagination.page(), pagination.size());
    }

    @Transactional(readOnly = true)
    public PageResult<ProductResult> listByStatus(ProductStatus status, OffsetPagination pagination) {
        return search(null, status, pagination.page(), pagination.size());
    }

    @Transactional(readOnly = true)
    public PageResult<ProductResult> search(String merchantId, ProductStatus status, int page, int size) {
        return search(new ProductSearchCriteria(
                null, merchantId, status,
                List.of(), List.of(),
                null, null,
                Map.of(),
                ProductSearchCriteria.Sort.NEWEST,
                page, size)).page();
    }

    @Transactional(readOnly = true)
    public ProductSearchResult search(ProductSearchCriteria criteria) {
        // Try OpenSearch first — it carries facets and full-text. Fall through to
        // a JPA-only path when the index is unreachable so the storefront stays up.
        Optional<ProductSearchIndex.SearchHits> hitsOpt = searchIndex.search(criteria);
        if (hitsOpt.isPresent()) {
            ProductSearchIndex.SearchHits hits = hitsOpt.get();
            List<Product> products = productRepository.findByIdsPreserveOrder(hits.productIds());
            List<ProductResult> results = products.stream()
                    .map(productResultMapper::toResult)
                    .toList();
            ProductSearchResult.Facets facets = new ProductSearchResult.Facets(
                    hits.brandCounts(),
                    hits.categoryCounts(),
                    hits.attributeCounts(),
                    hits.priceMin() == null && hits.priceMax() == null
                            ? null
                            : new ProductSearchResult.PriceRange(hits.priceMin(), hits.priceMax()));
            return ProductSearchResult.of(results, criteria.page(), criteria.size(), hits.totalHits(), facets);
        }
        return ProductSearchResult.of(jpaSearchFallback(criteria));
    }

    @Transactional(readOnly = true)
    public PageResult<ProductResult> search(SearchProductsQuery query) {
        return search(query.merchantId(), query.status(), query.pagination().page(), query.pagination().size());
    }

    /** Pure JPA path used when OpenSearch is offline or the index is empty. */
    private PageResult<ProductResult> jpaSearchFallback(ProductSearchCriteria criteria) {
        var pagination = OffsetPagination.safe(criteria.page(), criteria.size());
        List<Product> products;
        if (criteria.merchantId() != null && !criteria.merchantId().isBlank()) {
            products = productRepository.findByMerchant(criteria.merchantId(), pagination);
            ProductStatus status = criteria.status();
            if (status != null) {
                products = products.stream()
                        .filter(p -> p.getStatus() == status)
                        .toList();
            }
        } else if (criteria.hasText()) {
            products = productRepository.searchPublished(criteria.q(), pagination.size(), pagination.offset());
        } else {
            products = productRepository.findPublished(pagination.size(), pagination.offset());
        }
        long totalElements;
        if (criteria.merchantId() != null && !criteria.merchantId().isBlank()) {
            totalElements = products.size();
        } else if (criteria.hasText()) {
            totalElements = productRepository.countSearchPublished(criteria.q());
        } else {
            totalElements = productRepository.countPublished();
        }
        List<ProductResult> results = products.stream()
                .map(productResultMapper::toResult)
                .toList();
        return new PageResult<>(results, criteria.page(), criteria.size(), totalElements);
    }

    private static BigDecimal applyChange(BigDecimal oldAmount, BulkPriceUpdateCommand command) {
        return switch (command.changeType()) {
            case SET -> command.value();
            case INCREASE_AMOUNT -> oldAmount.add(command.value());
            case DECREASE_AMOUNT -> oldAmount.subtract(command.value()).max(BigDecimal.ZERO);
            case INCREASE_PERCENT -> oldAmount.add(oldAmount.multiply(command.value()).movePointLeft(2));
            case DECREASE_PERCENT -> oldAmount.subtract(oldAmount.multiply(command.value()).movePointLeft(2))
                    .max(BigDecimal.ZERO);
        };
    }

    private void reindexIfSearchable(Product product) {
        if (product.getStatus().isSearchable()) {
            searchIndex.index(buildSearchDocument(product));
        }
    }

    private ProductSearchDocument buildSearchDocument(Product product) {
        Map<String, String> filterable = new LinkedHashMap<>();
        if (!product.attributes().isEmpty()) {
            java.util.List<AttributeTemplate> templates =
                    attributeTemplateRepository.findByCategoryIds(product.categoryIds());
            for (AttributeTemplate template : templates) {
                for (Map.Entry<String, AttributeTemplate.AttributeDefinition> def : template.snapshot()
                        .entrySet()) {
                    if (def.getValue().filterable()) {
                        String value = product.attributes().get(def.getKey());
                        if (value != null) {
                            filterable.put(def.getKey(), value);
                        }
                    }
                }
            }
        }
        return productResultMapper.toSearchDocument(product, filterable);
    }

    @Transactional(readOnly = true)
    public void syncAllToSearchIndex() {
        log.info("Starting OpenSearch sync for all published products...");
        int limit = 100;
        int offset = 0;
        long totalSynced = 0;
        while (true) {
            List<Product> products = productRepository.findPublished(limit, offset);
            if (products.isEmpty()) {
                break;
            }
            List<ProductSearchDocument> docs = products.stream()
                    .map(this::buildSearchDocument)
                    .toList();
            searchIndex.indexAll(docs);
            totalSynced += products.size();
            offset += limit;
        }
        log.info("Successfully synced {} published products to OpenSearch.", totalSynced);
    }

    /** Resolves the caller's merchantId from their authenticated user id. */
    private String requireMerchantIdForOwner(String ownerId) {
        Merchant merchant = merchantRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.MERCHANT_NOT_FOUND,
                        "No merchant registered for the authenticated user"));
        return merchant.getMerchantId();
    }

    private Product ownedProduct(String productId, String ownerId) {
        String merchantId = requireMerchantIdForOwner(ownerId);
        Product product = required(productId);
        product.ensureOwnedBy(merchantId);
        if (product.getStatus() == ProductStatus.TAKEN_DOWN) {
            throw new CatalogException(CatalogErrorCode.PRODUCT_INVALID_TRANSITION,
                    "Product was taken down and cannot be modified");
        }
        return product;
    }

    private Product required(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.PRODUCT_NOT_FOUND));
    }

    private void publish(Product product) {
        eventPublisher.publish(product.pullEvents());
    }

    @Transactional(readOnly = true)
    public List<ProductResult> getRelatedProducts(String productId, int limit) {
        Product product = required(productId);
        List<Product> products = productRepository.findRelatedProducts(
                product.getProductId(),
                product.getBrandId(),
                product.categoryIds(),
                limit);
        return products.stream()
                .map(productResultMapper::toResult)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResult> getPopularProducts(int limit) {
        List<Product> products = productRepository.findPopularProducts(limit);
        return products.stream()
                .map(productResultMapper::toResult)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResult> getPersonalizedProducts(String userId, List<String> categoryIds, List<String> brandIds,
            int limit) {
        List<String> activeCategoryIds = categoryIds != null ? categoryIds : List.of();
        List<String> activeBrandIds = brandIds != null ? brandIds : List.of();

        if (userId != null && !userId.isBlank() && !userId.equals("anonymousUser")) {
            Optional<UserBrowsingHistory> history = userBrowsingHistoryRepository.findByUserId(userId);
            if (history.isPresent()) {
                activeCategoryIds = history.get().getCategoryIds();
                activeBrandIds = history.get().getBrandIds();
            }
        }

        if (activeCategoryIds.isEmpty() && activeBrandIds.isEmpty()) {
            return getPopularProducts(limit);
        }

        List<Product> products = productRepository.findPersonalizedProducts(activeCategoryIds, activeBrandIds, limit);
        if (products.isEmpty()) {
            return getPopularProducts(limit);
        }

        return products.stream()
                .map(productResultMapper::toResult)
                .toList();
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void trackProductView(String productId, String userId) {
        if (userId == null || userId.isBlank() || userId.equals("anonymousUser")) {
            return;
        }
        Product product = required(productId);
        UserBrowsingHistory history = userBrowsingHistoryRepository.findByUserId(userId)
                .orElseGet(() -> UserBrowsingHistory.create(userId));
        history.trackView(product.categoryIds(), product.getBrandId());
        userBrowsingHistoryRepository.save(history);
    }
}
