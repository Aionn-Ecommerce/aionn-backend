package com.aionn.promotion.domain.valueobject;

public enum CampaignType {
    /** Free shipping. */
    FREESHIP,
    /** Flat-amount discount on order subtotal. */
    DISCOUNT,
    /**
     * Time-bounded flash sale - vouchers can only be claimed inside the active
     * window.
     */
    FLASH_SALE,
    /** Percentage discount, capped by max amount. */
    PERCENT
}

