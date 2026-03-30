package com.ecommerce.identity.infrastructure.persistence.geography;

import com.ecommerce.identity.domain.geography.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {

    List<Country> findByActiveTrue();

    Optional<Country> findByCodeAndActiveTrue(String code);
}
