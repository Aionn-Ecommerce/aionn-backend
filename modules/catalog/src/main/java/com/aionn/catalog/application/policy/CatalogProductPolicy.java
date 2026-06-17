package com.aionn.catalog.application.policy;

public interface CatalogProductPolicy {

    String getDefaultCurrency();

    String getCloneNameSuffix();

    int getBulkMaxSize();

    int getReindexPageSize();
}
