package com.aionn.catalog.application.service;

import com.aionn.catalog.application.dto.product.command.ProductCommands;
import com.aionn.catalog.application.dto.product.result.ProductResult;
import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.application.mapper.ProductResultMapper;
import com.aionn.catalog.application.port.out.AttributeTemplateRepository;
import com.aionn.catalog.application.port.out.BrandRepository;
import com.aionn.catalog.application.port.out.CategoryRepository;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.catalog.application.port.out.ProductRepository;
import com.aionn.catalog.application.port.out.ProductSearchIndex;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.model.AttributeTemplate;
import com.aionn.catalog.domain.model.Brand;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.model.ProductVariant;
import com.aionn.catalog.domain.valueobject.BrandStatus;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.catalog.domain.valueobject.ProductStatus;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int MAX_BULK_SIZE = 5_000;

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeTemplateRepository attributeTemplateRepository;
    private final ProductResultMapper productResultMapper;
    private final ProductSearchIndex searchIndex;
    private final EventPublisher eventPublisher;

    // ===== UC3.13 / UC3.22 =====
    public ProductResult create(ProductCommands.CreateProduct command) {
        Product product = Product.create(IdGenerator.ulid(), command.merchantId(), command.name());
        Product saved = productRepository.save(product);
        publish(product);
        return productResultMapper.toResult(saved);
    }

    public ProductResult clone(ProductCommands.Clone command) {
        Product source = required(command.sourceId());
        source.ensureOwnedBy(command.merchantId());
        Product cloned = Product.create(IdGenerator.ulid(), command.merchantId(), source.getName() + " (Copy)");
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

    // ===== UC3.14 / UC3.23 =====
    public ProductResult defineVariant(ProductCommands.DefineVariant command) {
        Product product = ownedProduct(command.productId(), command.merchantId());
        Money price = command.price() == null
                ? null
                : Money.of(command.price(), command.currency() == null ? "VND" : command.currency());
        product.defineVariant(IdGenerator.ulid(), command.attributeValues(), price);
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    public ProductResult removeVariant(ProductCommands.RemoveVariant command) {
        Product product = ownedProduct(command.productId(), command.merchantId());
        product.removeVariant(command.skuId());
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    // ===== UC3.15 / UC3.16 / UC3.17 / UC3.25 / UC3.26 / UC3.32 =====
    public ProductResult updateMedia(ProductCommands.UpdateMedia command) {
        Product product = ownedProduct(command.productId(), command.merchantId());
        product.updateMedia(command.imageList());
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    public ProductResult assignBrand(ProductCommands.AssignBrand command) {
        Product product = ownedProduct(command.productId(), command.merchantId());
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

    public ProductResult categorize(ProductCommands.AssignCategories command) {
        Product product = ownedProduct(command.productId(), command.merchantId());
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

    public ProductResult updateAiMetadata(ProductCommands.UpdateAiMetadata command) {
        Product product = ownedProduct(command.productId(), command.merchantId());
        product.updateAiMetadata(command.tags(), command.aiDescription());
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    public ProductResult assignCollections(ProductCommands.AssignCollections command) {
        Product product = ownedProduct(command.productId(), command.merchantId());
        product.assignToCollections(command.collectionIds());
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    public ProductResult defineAttributes(ProductCommands.DefineAttributes command) {
        Product product = ownedProduct(command.productId(), command.merchantId());
        if (!product.categoryIds().isEmpty()) {
            // Check that every supplied key is declared in the template
            // attached to one of the assigned categories. If no template is
            // attached, accept any key (lets merchants experiment).
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

    // ===== UC3.18 / UC3.19 / UC3.20 / UC3.21 / UC3.28 =====
    public ProductResult publish(ProductCommands.Publish command) {
        Product product = required(command.productId());
        product.publish(command.adminId());
        Product saved = productRepository.save(product);
        publish(product);
        searchIndex.index(buildSearchDocument(saved));
        return productResultMapper.toResult(saved);
    }

    public ProductResult reject(ProductCommands.Reject command) {
        Product product = required(command.productId());
        product.reject(command.adminId(), command.reasonCode(), command.feedback());
        Product saved = productRepository.save(product);
        publish(product);
        searchIndex.remove(saved.getProductId());
        return productResultMapper.toResult(saved);
    }

    public ProductResult deactivate(ProductCommands.Deactivate command) {
        Product product = ownedProduct(command.productId(), command.merchantId());
        product.deactivate(command.reason());
        Product saved = productRepository.save(product);
        publish(product);
        searchIndex.remove(saved.getProductId());
        return productResultMapper.toResult(saved);
    }

    public ProductResult restore(ProductCommands.Restore command) {
        Product product = ownedProduct(command.productId(), command.merchantId());
        product.restore();
        Product saved = productRepository.save(product);
        publish(product);
        searchIndex.index(buildSearchDocument(saved));
        return productResultMapper.toResult(saved);
    }

    public ProductResult emergencyTakedown(ProductCommands.EmergencyTakedown command) {
        Product product = required(command.productId());
        product.emergencyTakedown(command.adminId(), command.reason());
        Product saved = productRepository.save(product);
        publish(product);
        searchIndex.remove(saved.getProductId());
        return productResultMapper.toResult(saved);
    }

    // ===== UC3.24 / UC3.27 / UC3.29 =====
    public ProductResult changeVariantPrice(ProductCommands.ChangeVariantPrice command) {
        Product product = ownedProduct(command.productId(), command.merchantId());
        product.changeVariantPrice(command.skuId(),
                Money.of(command.newPrice(), command.currency() == null ? "VND" : command.currency()));
        Product saved = productRepository.save(product);
        publish(product);
        reindexIfSearchable(saved);
        return productResultMapper.toResult(saved);
    }

    
    public void bulkPriceUpdate(ProductCommands.BulkPriceUpdate command) {
        if (command.skuIds() == null || command.skuIds().isEmpty()) {
            throw new CatalogException(CatalogErrorCode.INVALID_ARGUMENT, "skuIds must not be empty");
        }
        if (command.skuIds().size() > MAX_BULK_SIZE) {
            throw new CatalogException(CatalogErrorCode.PRODUCT_BULK_TOO_LARGE,
                    "Bulk size " + command.skuIds().size() + " exceeds max " + MAX_BULK_SIZE);
        }

        List<Product> affected = productRepository.findByMerchantAndSkuIds(command.merchantId(), command.skuIds());
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

    public ProductResult get(String productId) {
        return productResultMapper.toResult(required(productId));
    }

    private static BigDecimal applyChange(BigDecimal oldAmount, ProductCommands.BulkPriceUpdate command) {
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

    private Product ownedProduct(String productId, String merchantId) {
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

