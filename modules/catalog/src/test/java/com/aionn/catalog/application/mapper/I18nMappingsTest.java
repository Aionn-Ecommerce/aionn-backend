package com.aionn.catalog.application.mapper;

import com.aionn.catalog.application.dto.brand.result.BrandResult;
import com.aionn.catalog.application.dto.category.result.CategoryResult;
import com.aionn.catalog.application.dto.product.result.ProductResult;
import com.aionn.catalog.application.dto.search.ProductSearchDocument;
import com.aionn.catalog.domain.model.Brand;
import com.aionn.catalog.domain.model.Category;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.valueobject.BrandStatus;
import com.aionn.catalog.domain.valueobject.ProductStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import com.aionn.catalog.application.port.out.ProductReviewPersistencePort;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class I18nMappingsTest {

    private final ProductReviewPersistencePort reviewRepository = Mockito.mock(ProductReviewPersistencePort.class);
    private final ProductResultMapper productResultMapper = new ProductResultMapper(reviewRepository);
    private final CategoryResultMapper categoryResultMapper = new CategoryResultMapper();
    private final BrandResultMapper brandResultMapper = new BrandResultMapper();

    @BeforeEach
    void setUp() {
        LocaleContextHolder.resetLocaleContext();
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    @DisplayName("Should return Vietnamese translations when locale is vi")
    void shouldReturnVietnamese_whenLocaleIsVi() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("vi"));

        // Given
        Product product = new Product(
                "P_1", "M_1", "English Product", "B_1",
                List.of("C_1"), List.of("img.jpg"), List.of("tag"), List.of(),
                Map.of(), List.of(), "English AI Description", ProductStatus.PUBLISHED,
                Instant.now(), Instant.now(),
                List.of(new Product.Translation("vi", "Sản phẩm tiếng Việt", "Mô tả tiếng Việt"))
        );

        Category category = new Category(
                "C_1", null, "English Category", "eng-category", "icon.png", true,
                Instant.now(), Instant.now(), null,
                List.of(new Category.Translation("vi", "Danh mục tiếng Việt"))
        );

        Brand brand = new Brand(
                "B_1", "English Brand", "logo.png", "English description", BrandStatus.ACTIVE,
                Instant.now(), Instant.now(),
                List.of(new Brand.Translation("vi", "Thương hiệu tiếng Việt", "Mô tả thương hiệu tiếng Việt"))
        );

        // When
        ProductResult productResult = productResultMapper.toResult(product);
        ProductSearchDocument searchDoc = productResultMapper.toSearchDocument(product, Map.of());
        CategoryResult categoryResult = categoryResultMapper.toResult(category);
        BrandResult brandResult = brandResultMapper.toResult(brand);

        // Then
        assertEquals("Sản phẩm tiếng Việt", productResult.name());
        assertEquals("Mô tả tiếng Việt", productResult.aiDescription());
        assertEquals("Sản phẩm tiếng Việt", searchDoc.name());
        assertEquals("Mô tả tiếng Việt", searchDoc.aiDescription());

        assertEquals("Danh mục tiếng Việt", categoryResult.name());

        assertEquals("Thương hiệu tiếng Việt", brandResult.name());
        assertEquals("Mô tả thương hiệu tiếng Việt", brandResult.description());
    }

    @Test
    @DisplayName("Should return English default when locale is en or not matched")
    void shouldReturnDefault_whenLocaleIsEn() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // Given
        Product product = new Product(
                "P_1", "M_1", "English Product", "B_1",
                List.of("C_1"), List.of("img.jpg"), List.of("tag"), List.of(),
                Map.of(), List.of(), "English AI Description", ProductStatus.PUBLISHED,
                Instant.now(), Instant.now(),
                List.of(new Product.Translation("vi", "Sản phẩm tiếng Việt", "Mô tả tiếng Việt"))
        );

        Category category = new Category(
                "C_1", null, "English Category", "eng-category", "icon.png", true,
                Instant.now(), Instant.now(), null,
                List.of(new Category.Translation("vi", "Danh mục tiếng Việt"))
        );

        Brand brand = new Brand(
                "B_1", "English Brand", "logo.png", "English description", BrandStatus.ACTIVE,
                Instant.now(), Instant.now(),
                List.of(new Brand.Translation("vi", "Thương hiệu tiếng Việt", "Mô tả thương hiệu tiếng Việt"))
        );

        // When
        ProductResult productResult = productResultMapper.toResult(product);
        CategoryResult categoryResult = categoryResultMapper.toResult(category);
        BrandResult brandResult = brandResultMapper.toResult(brand);

        // Then
        assertEquals("English Product", productResult.name());
        assertEquals("English AI Description", productResult.aiDescription());
        assertEquals("English Category", categoryResult.name());
        assertEquals("English Brand", brandResult.name());
        assertEquals("English description", brandResult.description());
    }
}
