package com.aionn.ucp.application.service;

import com.aionn.ucp.application.dto.profile.BusinessProfileDto;
import com.aionn.ucp.application.dto.profile.CapabilityDeclaration;
import com.aionn.ucp.application.dto.profile.ServiceTransport;
import com.aionn.ucp.domain.model.CapabilityName;
import com.aionn.ucp.infrastructure.config.UcpProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessProfileService {

    private final UcpProperties properties;

    public BusinessProfileDto buildProfile() {
        String version = properties.getProtocolVersion();
        String specBase = properties.getSpecBaseUrl();
        String schemaBase = properties.getSchemaBaseUrl();
        String endpoint = trimTrailingSlash(properties.getEndpointBaseUrl()) + "/ucp/v1";

        Map<String, List<ServiceTransport>> services = new LinkedHashMap<>();
        services.put(CapabilityName.SERVICE_SHOPPING, List.of(
                new ServiceTransport(
                        version,
                        specBase + "/overview",
                        "rest",
                        endpoint,
                        schemaBase + "/services/shopping/rest.openapi.json")));

        Map<String, List<CapabilityDeclaration>> capabilities = new LinkedHashMap<>();
        if (properties.getCapabilities().isCatalogSearch()) {
            capabilities.put(CapabilityName.CATALOG_SEARCH, List.of(CapabilityDeclaration.of(
                    version,
                    specBase + "/catalog/search",
                    schemaBase + "/schemas/shopping/catalog_search.json")));
        }
        if (properties.getCapabilities().isCatalogLookup()) {
            capabilities.put(CapabilityName.CATALOG_LOOKUP, List.of(CapabilityDeclaration.of(
                    version,
                    specBase + "/catalog/lookup",
                    schemaBase + "/schemas/shopping/catalog_lookup.json")));
        }
        if (properties.getCapabilities().isCart()) {
            capabilities.put(CapabilityName.CART, List.of(CapabilityDeclaration.of(
                    version,
                    specBase + "/cart",
                    schemaBase + "/schemas/shopping/cart.json")));
        }
        if (properties.getCapabilities().isCheckout()) {
            capabilities.put(CapabilityName.CHECKOUT, List.of(CapabilityDeclaration.of(
                    version,
                    specBase + "/checkout",
                    schemaBase + "/schemas/shopping/checkout.json")));
        }
        if (properties.getCapabilities().isOrder()) {
            capabilities.put(CapabilityName.ORDER, List.of(CapabilityDeclaration.of(
                    version,
                    specBase + "/order",
                    schemaBase + "/schemas/shopping/order.json")));
        }
        if (properties.getCapabilities().isFulfillment()) {
            capabilities.put(CapabilityName.FULFILLMENT, List.of(new CapabilityDeclaration(
                    version,
                    specBase + "/fulfillment",
                    schemaBase + "/schemas/shopping/fulfillment.json",
                    CapabilityName.CHECKOUT,
                    null)));
        }
        if (properties.getCapabilities().isDiscount()) {
            capabilities.put(CapabilityName.DISCOUNT, List.of(new CapabilityDeclaration(
                    version,
                    specBase + "/discount",
                    schemaBase + "/schemas/shopping/discount.json",
                    CapabilityName.CHECKOUT,
                    null)));
        }
        if (properties.getCapabilities().isIdentityLinking()) {
            Map<String, Map<String, Object>> scopes = new LinkedHashMap<>();
            scopes.put("dev.ucp.shopping.order:read", Map.of());
            scopes.put("dev.ucp.shopping.order:manage", Map.of());
            capabilities.put(CapabilityName.IDENTITY_LINKING, List.of(new CapabilityDeclaration(
                    version,
                    specBase + "/identity-linking",
                    schemaBase + "/schemas/common/identity_linking.json",
                    null,
                    Map.of("scopes", scopes))));
        }

        BusinessProfileDto.UcpSection ucp = new BusinessProfileDto.UcpSection(
                version, services, capabilities, Map.of());

        List<BusinessProfileDto.SigningKey> signingKeys = null;
        if (properties.getSignature().getKeyId() != null
                && properties.getSignature().getPublicKeyPem() != null
                && !properties.getSignature().getPublicKeyPem().isBlank()) {
            signingKeys = List.of(new BusinessProfileDto.SigningKey(
                    properties.getSignature().getKeyId(),
                    "EC", "P-256", null, null, "sig", "ES256"));
        }
        return new BusinessProfileDto(ucp, signingKeys);
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
