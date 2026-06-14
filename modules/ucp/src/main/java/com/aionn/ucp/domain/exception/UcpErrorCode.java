package com.aionn.ucp.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UcpErrorCode {

    // Discovery / negotiation
    INVALID_PROFILE_URL("UCP_001", "Profile URL is malformed or missing"),
    PROFILE_UNREACHABLE("UCP_002", "Could not fetch the platform profile"),
    PROFILE_MALFORMED("UCP_003", "Platform profile is not valid JSON"),
    VERSION_UNSUPPORTED("UCP_004", "Protocol version is not supported"),
    CAPABILITIES_INCOMPATIBLE("UCP_005", "No compatible capabilities"),

    // Catalog
    CATALOG_REQUEST_INVALID("UCP_101", "Catalog request must contain a query or filters or ids"),
    CATALOG_REQUEST_TOO_LARGE("UCP_102", "Catalog request exceeds the supported batch size"),
    CATALOG_PRODUCT_NOT_FOUND("UCP_103", "Product not found"),

    // Checkout
    CHECKOUT_LINE_ITEMS_REQUIRED("UCP_201", "Checkout requires at least one line item"),
    CHECKOUT_NOT_FOUND("UCP_202", "Checkout session not found"),
    CHECKOUT_INVALID_STATE("UCP_203", "Checkout session is not in a state that allows this operation"),
    CHECKOUT_FORBIDDEN("UCP_204", "Checkout session does not belong to caller"),

    // Order
    ORDER_NOT_FOUND("UCP_301", "Order not found"),

    // Cart
    CART_NOT_FOUND("UCP_401", "Cart session not found"),
    CART_FORBIDDEN("UCP_402", "Cart session does not belong to caller"),

    // Generic
    INVALID_ARGUMENT("UCP_900", "Invalid argument");

    private final String code;
    private final String defaultMessage;
}
