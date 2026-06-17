package com.aionn.catalog.adapter.rest.controller;

import com.aionn.catalog.adapter.rest.dto.brand.CreateBrandRequest;
import com.aionn.catalog.adapter.rest.dto.brand.DeleteBrandRequest;
import com.aionn.catalog.adapter.rest.exception.CatalogExceptionHandler;
import com.aionn.catalog.application.dto.brand.command.CreateBrandCommand;
import com.aionn.catalog.application.dto.brand.command.DeleteBrandCommand;
import com.aionn.catalog.application.dto.brand.result.BrandResult;
import com.aionn.catalog.application.service.BrandService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BrandControllerWebTest {

    @Mock
    private BrandService brandService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @BeforeEach
    void setUp() {
        BrandController controller = new BrandController(brandService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new CatalogExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private BrandResult sampleBrand() {
        return new BrandResult("b-1", "Acme", null, "desc", "ACTIVE",
                Instant.now(), Instant.now());
    }

    @Test
    void createReturnsCreatedAndDelegatesToService() throws Exception {
        when(brandService.create(any(CreateBrandCommand.class))).thenReturn(sampleBrand());

        mockMvc.perform(post("/api/v1/catalog/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateBrandRequest("Acme", null, "desc"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.brandId").value("b-1"))
                .andExpect(jsonPath("$.data.name").value("Acme"));

        verify(brandService).create(any(CreateBrandCommand.class));
    }

    @Test
    void getReturnsBrand() throws Exception {
        when(brandService.get("b-1")).thenReturn(sampleBrand());

        mockMvc.perform(get("/api/v1/catalog/brands/b-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.brandId").value("b-1"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(brandService).get("b-1");
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/v1/catalog/brands/b-1/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeleteBrandRequest("policy"))))
                .andExpect(status().isNoContent());

        verify(brandService).delete(any(DeleteBrandCommand.class));
    }

    @Test
    void getReturnsNotFoundWhenServiceThrowsBrandNotFound() throws Exception {
        when(brandService.get("missing"))
                .thenThrow(new CatalogException(CatalogErrorCode.BRAND_NOT_FOUND));

        mockMvc.perform(get("/api/v1/catalog/brands/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturnsConflictWhenBrandHasActiveProducts() throws Exception {
        doThrow(new CatalogException(CatalogErrorCode.BRAND_HAS_ACTIVE_PRODUCTS))
                .when(brandService).delete(any(DeleteBrandCommand.class));

        mockMvc.perform(post("/api/v1/catalog/brands/b-1/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeleteBrandRequest("policy"))))
                .andExpect(status().isConflict());
    }
}
