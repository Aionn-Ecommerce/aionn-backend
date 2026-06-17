package com.aionn.catalog.adapter.rest.controller;

import com.aionn.catalog.adapter.rest.dto.merchant.AdminReasonRequest;
import com.aionn.catalog.adapter.rest.dto.merchant.RegisterMerchantRequest;
import com.aionn.catalog.adapter.rest.exception.CatalogExceptionHandler;
import com.aionn.catalog.adapter.rest.support.MockSecurityInterceptor;
import com.aionn.catalog.adapter.rest.support.TestAuth;
import com.aionn.catalog.adapter.rest.support.session.CurrentAdminIdArgumentResolver;
import com.aionn.catalog.adapter.rest.support.session.CurrentOwnerIdArgumentResolver;
import com.aionn.catalog.application.dto.merchant.command.RegisterMerchantCommand;
import com.aionn.catalog.application.dto.merchant.command.SuspendMerchantCommand;
import com.aionn.catalog.application.dto.merchant.result.MerchantResult;
import com.aionn.catalog.application.service.MerchantService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MerchantControllerWebTest {

    @Mock
    private MerchantService merchantService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @BeforeEach
    void setUp() {
        MerchantController controller = new MerchantController(merchantService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new CatalogExceptionHandler())
                .setCustomArgumentResolvers(
                        new CurrentOwnerIdArgumentResolver(),
                        new CurrentAdminIdArgumentResolver())
                .addInterceptors(new MockSecurityInterceptor())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private MerchantResult sample() {
        return new MerchantResult(
                "m-1", "owner-1", "Acme", null, "desc", "01", "Ha Noi",
                "ACTIVE", Instant.now(), Instant.now());
    }

    @Test
    void registerReturnsCreatedAndPassesPrincipalToService() throws Exception {
        when(merchantService.register(any(RegisterMerchantCommand.class))).thenReturn(sample());

        mockMvc.perform(post("/api/v1/catalog/merchants")
                        .with(TestAuth.authUser("owner-1", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterMerchantRequest("Acme Store"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.merchantId").value("m-1"))
                .andExpect(jsonPath("$.data.ownerId").value("owner-1"));

        verify(merchantService).register(any(RegisterMerchantCommand.class));
    }

    @Test
    void getReturnsMerchant() throws Exception {
        when(merchantService.get("m-1")).thenReturn(sample());

        mockMvc.perform(get("/api/v1/catalog/merchants/m-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.merchantId").value("m-1"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void getMineResolvesByOwnerId() throws Exception {
        when(merchantService.getByOwner("owner-1")).thenReturn(sample());

        mockMvc.perform(get("/api/v1/catalog/merchants/me")
                        .with(TestAuth.authUser("owner-1", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ownerId").value("owner-1"));

        verify(merchantService).getByOwner("owner-1");
    }

    @Test
    void suspendDelegatesAdminReasonToService() throws Exception {
        when(merchantService.suspend(any(SuspendMerchantCommand.class))).thenReturn(sample());

        mockMvc.perform(post("/api/v1/catalog/merchants/m-1/suspend")
                        .with(TestAuth.authUser("admin-1", "SYSTEM_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminReasonRequest("policy violation"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.merchantId").value("m-1"));

        verify(merchantService).suspend(any(SuspendMerchantCommand.class));
    }

    @Test
    void getReturnsNotFoundWhenServiceThrows() throws Exception {
        when(merchantService.get("missing"))
                .thenThrow(new CatalogException(CatalogErrorCode.MERCHANT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/catalog/merchants/missing"))
                .andExpect(status().isNotFound());
    }
}
