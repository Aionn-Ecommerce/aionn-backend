package com.aionn.catalog.infrastructure.adapter;

import com.aionn.catalog.application.port.out.BrandRepository;
import com.aionn.catalog.domain.model.Brand;
import com.aionn.catalog.domain.valueobject.ProductStatus;
import com.aionn.catalog.infrastructure.persistence.mapper.BrandDomainMapper;
import com.aionn.catalog.infrastructure.persistence.repository.BrandJpaRepository;
import com.aionn.catalog.infrastructure.persistence.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BrandRepositoryAdapter implements BrandRepository {

    private final BrandJpaRepository jpa;
    private final ProductJpaRepository productJpa;
    private final BrandDomainMapper mapper;

    @Override
    public Brand save(Brand brand) {
        return mapper.toDomain(jpa.save(mapper.toEntity(brand)));
    }

    @Override
    public Optional<Brand> findById(String brandId) {
        return jpa.findById(brandId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpa.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean hasActiveProducts(String brandId) {
        return productJpa.existsByBrandIdAndStatus(brandId, ProductStatus.PUBLISHED.name());
    }
}

