package com.aionn.sharedkernel.integration.port.catalog;

import java.util.Optional;

public interface MerchantQueryPort {

    Optional<String> findMerchantIdByOwnerId(String ownerId);
}
