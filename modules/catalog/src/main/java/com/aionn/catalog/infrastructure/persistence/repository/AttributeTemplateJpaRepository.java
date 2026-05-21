package com.aionn.catalog.infrastructure.persistence.repository;

import com.aionn.catalog.infrastructure.persistence.entity.AttributeTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttributeTemplateJpaRepository extends JpaRepository<AttributeTemplateEntity, String> {

    Optional<AttributeTemplateEntity> findByCategoryId(String categoryId);
}

