package com.aionn.catalog.application.port.out;

/**
 * Bridge to the Ordering bounded context. We need to know if a merchant has
 * pending orders before allowing permanent closure. Two implementations are
 * provided per project convention: an "assume-empty" stub for dev/test and a
 * real adapter that queries the Ordering module / external service when the
 * integration is wired up.
 */
public interface OpenOrderQueryPort {

    boolean hasOpenOrdersForMerchant(String merchantId);
}

