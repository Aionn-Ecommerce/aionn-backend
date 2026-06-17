package com.aionn.catalog.application.port.out.brand;

import com.aionn.catalog.domain.model.Brand;

import java.util.List;
import java.util.Optional;

import com.aionn.sharedkernel.domain.vo.OffsetPagination;

public interface BrandRepositoryPort {

    Brand save(Brand brand);

    Optional<Brand> findById(String brandId);

    boolean existsByName(String name);

    boolean hasActiveProducts(String brandId);

    List<Brand> list(OffsetPagination pagination);
}
