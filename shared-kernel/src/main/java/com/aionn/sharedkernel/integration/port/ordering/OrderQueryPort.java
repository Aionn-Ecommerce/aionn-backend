package com.aionn.sharedkernel.integration.port.ordering;

public interface OrderQueryPort {

    boolean hasOpenOrdersForMerchant(String merchantId);
}
