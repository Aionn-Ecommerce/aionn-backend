package com.aionn.sharedkernel.integration.port.ordering;

import java.util.Collection;

public interface OrderQueryPort {

    boolean hasOpenOrdersForMerchant(String merchantId);

    boolean hasCompletedPurchaseForSkus(String userId, Collection<String> skuIds);

    String findCompletedOrderIdForSkus(String userId, Collection<String> skuIds);
}
