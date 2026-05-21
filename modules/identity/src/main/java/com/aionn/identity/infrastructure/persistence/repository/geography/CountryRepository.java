package com.aionn.identity.infrastructure.persistence.repository.geography;

import com.aionn.identity.domain.geography.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {

    List<Country> findByActiveTrue();

    Optional<Country> findByCodeAndActiveTrue(String code);
}

