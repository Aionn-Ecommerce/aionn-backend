package com.aionn.catalog.adapter.rest.controller;

import com.aionn.catalog.adapter.rest.dto.product.CreateProductRequest;
import com.aionn.catalog.adapter.rest.exception.CatalogExceptionHandler;
import com.aionn.catalog.adapter.rest.support.MockSecurityInterceptor;
import com.aionn.catalog.adapter.rest.support.TestAuth;
import com.aionn.catalog.adapter.rest.support.session.CurrentAdminIdArgumentResolver;
import com.aionn.catalog.adapter.rest.support.session.CurrentOwnerIdArgumentResolver;
import com.aionn.catalog.application.dto.common.PageResult;
import com.aionn.catalog.application.dto.product.command.CreateProductCommand;
import com.aionn.catalog.application.dto.product.command.PublishCommand;
import com.aionn.catalog.application.dto.product.result.ProductResult;
import com.aionn.catalog.application.service.ProductService;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerWebTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @BeforeEach
    void setUp() {
        ProductController controller = new ProductController(productService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new CatalogExceptionHandler())
                .setCustomArgumentResolvers(
                        new CurrentOwnerIdArgumentResolver(),
                        new CurrentAdminIdArgumentResolver())
                .addInterceptors(new MockSecurityInterceptor())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private ProductResult sample(String id) {
        return new ProductResult(
                id, "m-1", "Phone", null,
                List.of(), List.of(), List.of(), List.of(), Map.of(), List.of(),
                null, "DRAFT", Instant.now(), Instant.now(),
                0.0, 0L, 0L, null, null, null);
    }

    @Test
    void createReturnsCreatedAndForwardsOwnerId() throws Exception {
        when(productService.create(any(CreateProductCommand.class))).thenReturn(sample("p-1"));

        mockMvc.perform(post("/api/v1/catalog/products")
                        .with(TestAuth.authUser("owner-1", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateProductRequest("Phone"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.productId").value("p-1"))
                .andExpect(jsonPath("$.data.merchantId").value("m-1"));

        verify(productService).create(any(CreateProductCommand.class));
    }

    @Test
    void getReturnsProduct() throws Exception {
        when(productService.get("p-1")).thenReturn(sample("p-1"));

        mockMvc.perform(get("/api/v1/catalog/products/p-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value("p-1"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void listByMerchantReturnsPagedResult() throws Exception {
        PageResult<ProductResult> page = new PageResult<>(List.of(sample("p-1")), 0, 20, 1L);
        when(productService.listByMerchant("m-1", 0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/v1/catalog/products").param("merchantId", "m-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].productId").value("p-1"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void publishReturnsOkWhenAdminAuthenticated() throws Exception {
        when(productService.publish(any(PublishCommand.class))).thenReturn(sample("p-1"));

        mockMvc.perform(post("/api/v1/catalog/products/p-1/publish")
                        .with(TestAuth.authUser("admin-1", "SYSTEM_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value("p-1"));

        verify(productService).publish(any(PublishCommand.class));
    }

    @Test
    void getReturnsNotFoundWhenServiceThrows() throws Exception {
        when(productService.get("missing"))
                .thenThrow(new CatalogException(CatalogErrorCode.PRODUCT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/catalog/products/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void resolveBySkuIdsReturnsProducts() throws Exception {
        when(productService.getBySkuIds(List.of("sku-1", "sku-2")))
                .thenReturn(List.of(sample("p-1"), sample("p-2")));

        mockMvc.perform(get("/api/v1/catalog/products/resolve-by-skus")
                        .param("skuIds", "sku-1", "sku-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].productId").value("p-1"));
    }
}
