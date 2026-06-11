package com.aionn.catalog.domain;

/**
 * Catalog-wide business invariants. Keep numbers in one place so the
 * application service that enforces them and the request DTO that
 * validates them stay in lock-step.
 */
public final class CatalogLimits {

    /**
     * Maximum number of SKUs that can be addressed by a single bulk
     * price-update command. Larger requests are rejected to keep the
     * transaction footprint bounded.
     */
    public static final int BULK_PRICE_UPDATE_MAX_SIZE = 5_000;

    private CatalogLimits() {
    }
}
