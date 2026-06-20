package com.aionn.catalog.infrastructure.persistence.adapter.brand;

import com.aionn.catalog.application.port.out.BrandPersistencePort;
import com.aionn.catalog.domain.model.Brand;
import com.aionn.catalog.domain.valueobject.ProductStatus;
import com.aionn.catalog.infrastructure.persistence.mapper.BrandDomainMapper;
import com.aionn.catalog.infrastructure.persistence.repository.BrandRepository;
import com.aionn.catalog.infrastructure.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BrandPersistenceAdapter implements BrandPersistencePort {

    private final BrandRepository jpa;
    private final ProductRepository productJpa;
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

    @Override
    public java.util.List<Brand> findAll(int page, int size) {
        return jpa.findAll(org.springframework.data.domain.PageRequest.of(page, size)).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return jpa.count();
    }
}

