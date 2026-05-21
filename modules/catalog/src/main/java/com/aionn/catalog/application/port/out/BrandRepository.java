package com.aionn.catalog.application.port.out;

import com.aionn.catalog.domain.model.Brand;

import java.util.Optional;

public interface BrandRepository {

    Brand save(Brand brand);

    Optional<Brand> findById(String brandId);

    boolean existsByName(String name);

    boolean hasActiveProducts(String brandId);
}

