package com.ecommerce.identity.infrastructure.persistence.geography;

import com.ecommerce.identity.domain.geography.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WardRepository extends JpaRepository<Ward, String> {

    List<Ward> findByDistrictCodeAndActiveTrue(String districtCode);

    Optional<Ward> findByCodeAndActiveTrue(String code);
}
