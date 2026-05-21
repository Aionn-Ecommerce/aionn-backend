package com.aionn.chat.domain.valueobject;

/**
 * Shopee-style message variants.
 *
 * <ul>
 * <li>{@code TEXT} - plain text body.</li>
 * <li>{@code IMAGE} - image url + thumbnail.</li>
 * <li>{@code PRODUCT_CARD} - shared product reference (catalog snapshot).</li>
 * <li>{@code ORDER_REF} - shared order reference (id + summary).</li>
 * <li>{@code SYSTEM} - system-generated banner (greeting, auto-reply
 * marker).</li>
 * </ul>
 */
public enum MessageType {
    TEXT,
    IMAGE,
    PRODUCT_CARD,
    ORDER_REF,
    SYSTEM
}

