package com.aionn.catalog.adapter.rest.controller;

import com.aionn.catalog.adapter.rest.dto.attribute.ConfigureFilterableRequest;
import com.aionn.catalog.adapter.rest.dto.attribute.CreateAttributeTemplateRequest;
import com.aionn.catalog.adapter.rest.exception.CatalogExceptionHandler;
import com.aionn.catalog.application.dto.attribute.command.ConfigureFilterableCommand;
import com.aionn.catalog.application.dto.attribute.command.CreateAttributeTemplateCommand;
import com.aionn.catalog.application.dto.attribute.result.AttributeTemplateResult;
import com.aionn.catalog.application.service.AttributeTemplateService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AttributeTemplateControllerWebTest {

    @Mock
    private AttributeTemplateService attributeTemplateService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @BeforeEach
    void setUp() {
        AttributeTemplateController controller = new AttributeTemplateController(attributeTemplateService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new CatalogExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private AttributeTemplateResult sample() {
        Map<String, Boolean> attrs = new LinkedHashMap<>();
        attrs.put("color", true);
        attrs.put("size", true);
        return new AttributeTemplateResult("t-1", "c-1", attrs, Instant.now(), Instant.now());
    }

    @Test
    void createReturnsCreatedAndIncludesAttributes() throws Exception {
        when(attributeTemplateService.create(any(CreateAttributeTemplateCommand.class))).thenReturn(sample());

        mockMvc.perform(post("/api/v1/catalog/attribute-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateAttributeTemplateRequest("c-1", List.of("color", "size")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.templateId").value("t-1"))
                .andExpect(jsonPath("$.data.categoryId").value("c-1"))
                .andExpect(jsonPath("$.data.attributes.color").value(true));

        verify(attributeTemplateService).create(any(CreateAttributeTemplateCommand.class));
    }

    @Test
    void getReturnsTemplate() throws Exception {
        when(attributeTemplateService.get("t-1")).thenReturn(sample());

        mockMvc.perform(get("/api/v1/catalog/attribute-templates/t-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.templateId").value("t-1"));
    }

    @Test
    void configureFilterableUpdatesAttribute() throws Exception {
        when(attributeTemplateService.configureFilterable(any(ConfigureFilterableCommand.class)))
                .thenReturn(sample());

        mockMvc.perform(put("/api/v1/catalog/attribute-templates/t-1/filterable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ConfigureFilterableRequest("color", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.templateId").value("t-1"));

        verify(attributeTemplateService).configureFilterable(any(ConfigureFilterableCommand.class));
    }

    @Test
    void getReturnsNotFoundWhenServiceThrows() throws Exception {
        when(attributeTemplateService.get("missing"))
                .thenThrow(new CatalogException(CatalogErrorCode.ATTRIBUTE_TEMPLATE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/catalog/attribute-templates/missing"))
                .andExpect(status().isNotFound());
    }
}
