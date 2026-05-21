package com.aionn.identity.infrastructure.persistence.repository.geography;

import com.aionn.identity.domain.geography.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, String> {

    List<District> findByProvinceCodeAndActiveTrue(String provinceCode);

    Optional<District> findByCodeAndActiveTrue(String code);
}

