package com.aionn.sharedkernel.integration.port.catalog;

import java.util.Optional;

/**
 * Cross-context read port: resolves the catalog merchant owned by the given
 * authenticated user. Implemented in the catalog module so other modules
 * (inventory, ordering) can verify merchant-side authorization without
 * pulling the catalog persistence layer onto their classpath.
 */
public interface MerchantQueryPort {

    /**
     * Returns the {@code merchantId} owned by {@code ownerId} (the
     * authenticated user's id), or empty if the caller has not registered
     * a merchant yet.
     */
    Optional<String> findMerchantIdByOwnerId(String ownerId);
}
