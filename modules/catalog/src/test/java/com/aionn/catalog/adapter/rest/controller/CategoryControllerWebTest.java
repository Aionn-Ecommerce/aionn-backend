package com.aionn.catalog.adapter.rest.controller;

import com.aionn.catalog.adapter.rest.dto.category.CreateCategoryRequest;
import com.aionn.catalog.adapter.rest.exception.CatalogExceptionHandler;
import com.aionn.catalog.application.dto.category.command.CreateCategoryCommand;
import com.aionn.catalog.application.dto.category.result.CategoryResult;
import com.aionn.catalog.application.dto.category.result.CategoryTreeNode;
import com.aionn.catalog.application.service.CategoryService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerWebTest {

    @Mock
    private CategoryService categoryService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @BeforeEach
    void setUp() {
        CategoryController controller = new CategoryController(categoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new CatalogExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private CategoryResult sample(String id) {
        return new CategoryResult(id, null, "Electronics", "electronics", null, true,
                Instant.now(), Instant.now());
    }

    @Test
    void createReturnsCreatedAndDelegatesToService() throws Exception {
        when(categoryService.create(any(CreateCategoryCommand.class))).thenReturn(sample("c-1"));

        mockMvc.perform(post("/api/v1/catalog/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCategoryRequest(null, "Electronics", "electronics"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.categoryId").value("c-1"))
                .andExpect(jsonPath("$.data.slug").value("electronics"));

        verify(categoryService).create(any(CreateCategoryCommand.class));
    }

    @Test
    void getReturnsCategory() throws Exception {
        when(categoryService.get("c-1")).thenReturn(sample("c-1"));

        mockMvc.perform(get("/api/v1/catalog/categories/c-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryId").value("c-1"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void listRootsReturnsArray() throws Exception {
        when(categoryService.listRoots()).thenReturn(List.of(sample("c-1"), sample("c-2")));

        mockMvc.perform(get("/api/v1/catalog/categories/roots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void treeReturnsNestedStructure() throws Exception {
        CategoryTreeNode child = new CategoryTreeNode(sample("c-2"), List.of());
        CategoryTreeNode root = new CategoryTreeNode(sample("c-1"), List.of(child));
        when(categoryService.tree()).thenReturn(List.of(root));

        mockMvc.perform(get("/api/v1/catalog/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].category.categoryId").value("c-1"))
                .andExpect(jsonPath("$.data[0].children[0].category.categoryId").value("c-2"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/catalog/categories/c-1"))
                .andExpect(status().isNoContent());

        verify(categoryService).delete("c-1");
    }

    @Test
    void getReturnsNotFoundWhenServiceThrows() throws Exception {
        when(categoryService.get("missing"))
                .thenThrow(new CatalogException(CatalogErrorCode.CATEGORY_NOT_FOUND));

        mockMvc.perform(get("/api/v1/catalog/categories/missing"))
                .andExpect(status().isNotFound());
    }
}
