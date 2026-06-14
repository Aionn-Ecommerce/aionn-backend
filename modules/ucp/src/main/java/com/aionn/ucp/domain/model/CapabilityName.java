package com.aionn.ucp.domain.model;

public final class CapabilityName {

    private CapabilityName() {
    }

    public static final String SERVICE_SHOPPING = "dev.ucp.shopping";

    public static final String CATALOG_SEARCH = "dev.ucp.shopping.catalog.search";
    public static final String CATALOG_LOOKUP = "dev.ucp.shopping.catalog.lookup";
    public static final String CART = "dev.ucp.shopping.cart";
    public static final String CHECKOUT = "dev.ucp.shopping.checkout";
    public static final String ORDER = "dev.ucp.shopping.order";

    public static final String FULFILLMENT = "dev.ucp.shopping.fulfillment";
    public static final String DISCOUNT = "dev.ucp.shopping.discount";

    public static final String IDENTITY_LINKING = "dev.ucp.common.identity_linking";
}
