package com.aionn.catalog.infrastructure.persistence.repository;

import com.aionn.catalog.infrastructure.persistence.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<BrandEntity, String> {

    boolean existsByNameIgnoreCase(String name);
}

