package com.aionn.catalog.application.service;

import com.aionn.catalog.application.dto.product.command.AssignBrandCommand;
import com.aionn.catalog.application.dto.product.command.AssignCategoriesCommand;
import com.aionn.catalog.application.dto.product.command.AssignCollectionsCommand;
import com.aionn.catalog.application.dto.product.command.BulkPriceUpdateCommand;
import com.aionn.catalog.application.dto.product.command.ChangeVariantPriceCommand;
import com.aionn.catalog.application.dto.product.command.CloneCommand;
import com.aionn.catalog.application.dto.product.command.CreateProductCommand;
import com.aionn.catalog.application.dto.product.command.DeactivateCommand;
import com.aionn.catalog.application.dto.product.command.DefineAttributesCommand;
import com.aionn.catalog.application.dto.product.command.DefineVariantCommand;
import com.aionn.catalog.application.dto.product.command.EmergencyTakedownCommand;
import com.aionn.catalog.application.dto.product.command.PublishCommand;
import com.aionn.catalog.application.dto.product.command.RejectCommand;
import com.aionn.catalog.application.dto.product.command.RemoveVariantCommand;
import com.aionn.catalog.application.dto.product.command.RestoreCommand;
import com.aionn.catalog.application.dto.product.command.UpdateAiMetadataCommand;
import com.aionn.catalog.application.dto.product.command.UpdateMediaCommand;
import com.aionn.catalog.application.dto.product.result.ProductResult;
import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.application.mapper.ProductResultMapper;
import com.aionn.catalog.application.port.out.AttributeTemplateRepository;
import com.aionn.catalog.application.port.out.BrandRepository;
import com.aionn.catalog.application.port.out.CategoryRepository;
import com.aionn.catalog.application.port.out.MerchantRepository;
import com.aionn.catalog.application.port.out.ProductRepository;
import com.aionn.catalog.application.port.out.ProductSearchIndex;
import com.aionn.catalog.domain.CatalogLimits;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.model.AttributeTemplate;
import com.aionn.catalog.domain.model.Brand;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MerchantRepository merchantRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeTemplateRepository attributeTemplateRepository;
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
        for (String categoryId : command.categoryIds()) {
            categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CatalogException(CatalogErrorCode.CATEGORY_NOT_FOUND,
                            "Unknown category: " + categoryId));
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
            for (String categoryId : product.categoryIds()) {
                attributeTemplateRepository.findByCategoryId(categoryId).ifPresent(template -> {
                    for (String key : command.attributes().keySet()) {
                        if (!template.snapshot().containsKey(key)) {
                            throw new CatalogException(CatalogErrorCode.ATTRIBUTE_KEY_NOT_FOUND,
                                    "Attribute '" + key + "' is not declared on the category template");
                        }
                    }
                });
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

    public ProductResult reject(RejectCommand command) {
        Product product = required(command.productId());
        product.reject(command.adminId(), command.reasonCode(), command.feedback());
        Product saved = productRepository.save(product);
        publish(product);
        searchIndex.remove(saved.getProductId());
        return productResultMapper.toResult(saved);
    }

    public ProductResult deactivate(DeactivateCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        product.deactivate(command.reason());
        Product saved = productRepository.save(product);
        publish(product);
        searchIndex.remove(saved.getProductId());
        return productResultMapper.toResult(saved);
    }

    public ProductResult restore(RestoreCommand command) {
        Product product = ownedProduct(command.productId(), command.ownerId());
        product.restore();
        Product saved = productRepository.save(product);
        publish(product);
        searchIndex.index(buildSearchDocument(saved));
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

    public void bulkPriceUpdate(BulkPriceUpdateCommand command) {
        if (command.skuIds() == null || command.skuIds().isEmpty()) {
            throw new CatalogException(CatalogErrorCode.INVALID_ARGUMENT, "skuIds must not be empty");
        }
        if (command.skuIds().size() > CatalogLimits.BULK_PRICE_UPDATE_MAX_SIZE) {
            throw new CatalogException(CatalogErrorCode.PRODUCT_BULK_TOO_LARGE,
                    "Bulk size " + command.skuIds().size() + " exceeds max "
                            + CatalogLimits.BULK_PRICE_UPDATE_MAX_SIZE);
        }

        String merchantId = requireMerchantIdForOwner(command.ownerId());
        List<Product> affected = productRepository.findByMerchantAndSkuIds(merchantId, command.skuIds());
        if (affected.isEmpty()) {
            log.warn("Bulk price update by owner={} matched 0 products for {} skuIds (none owned by merchant {})",
                    command.ownerId(), command.skuIds().size(), merchantId);
            return;
        }
        for (Product product : affected) {
            for (ProductVariant variant : product.variants()) {
                if (!command.skuIds().contains(variant.skuId())) {
                    continue;
                }
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
        }
    }

    @Transactional(readOnly = true)
    public ProductResult get(String productId) {
        return productResultMapper.toResult(required(productId));
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
            for (String categoryId : product.categoryIds()) {
                attributeTemplateRepository.findByCategoryId(categoryId).ifPresent(template -> {
                    for (Map.Entry<String, AttributeTemplate.AttributeDefinition> def : template.snapshot()
                            .entrySet()) {
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

    /**
     * Resolves the caller's {@code merchantId} from their authenticated user id.
     */
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
}
