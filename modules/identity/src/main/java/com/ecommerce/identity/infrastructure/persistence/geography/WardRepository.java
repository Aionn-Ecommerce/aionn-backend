package com.ecommerce.identity.infrastructure.persistence.geography;

import com.ecommerce.identity.domain.geography.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WardRepository extends JpaRepository<Ward, String> {

    List<Ward> findByDistrictCodeAndActiveTrue(String districtCode);

    Optional<Ward> findByCodeAndActiveTrue(String code);

    /**
     * Fetch ward with district and province in a SINGLE query using JOIN FETCH.
     * This avoids N+1 queries when resolving complete location.
     * Returns ward with district and province eagerly loaded.
     */
    @Query("SELECT w FROM Ward w " +
            "JOIN FETCH w.district d " +
            "JOIN FETCH d.province p " +
            "WHERE w.code = :code AND w.active = true")
    Optional<Ward> findByCodeWithDistrictAndProvince(@Param("code") String code);
}
