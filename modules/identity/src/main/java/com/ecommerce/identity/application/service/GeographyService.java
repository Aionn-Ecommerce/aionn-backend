package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.geography.result.GeographyResult;
import com.ecommerce.identity.application.dto.geography.result.ResolvedLocation;
import com.ecommerce.identity.application.port.out.geography.GeographyPersistencePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing geography data (countries, provinces, districts, wards).
 * Provides read-only access to hierarchical geography information with caching
 * support.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeographyService {

    private final GeographyPersistencePort geographyPersistencePort;

    public List<GeographyResult> listCountries() {
        log.debug("Listing all active countries");
        return geographyPersistencePort.findAllCountries();
    }

    @Cacheable(value = "geography", key = "'country:' + #code")
    public Optional<GeographyResult> getCountry(String code) {
        log.debug("Getting country by code: {}", code);
        return geographyPersistencePort.findCountryByCode(code);
    }

    public List<GeographyResult> listProvinces(String countryCode) {
        log.debug("Listing provinces for country: {}", countryCode);
        if (countryCode != null) {
            return geographyPersistencePort.findProvincesByCountryCode(countryCode);
        }
        return geographyPersistencePort.findAllProvinces();
    }

    @Cacheable(value = "geography", key = "'province:' + #code")
    public Optional<GeographyResult> getProvince(String code) {
        log.debug("Getting province by code: {}", code);
        return geographyPersistencePort.findProvinceByCode(code);
    }

    public List<GeographyResult> listDistricts(String provinceCode) {
        log.debug("Listing districts for province: {}", provinceCode);
        return geographyPersistencePort.findDistrictsByProvinceCode(provinceCode);
    }

    @Cacheable(value = "geography", key = "'district:' + #code")
    public Optional<GeographyResult> getDistrict(String code) {
        log.debug("Getting district by code: {}", code);
        return geographyPersistencePort.findDistrictByCode(code);
    }

    public List<GeographyResult> listWards(String districtCode) {
        log.debug("Listing wards for district: {}", districtCode);
        return geographyPersistencePort.findWardsByDistrictCode(districtCode);
    }

    @Cacheable(value = "geography", key = "'ward:' + #code")
    public Optional<GeographyResult> getWard(String code) {
        log.debug("Getting ward by code: {}", code);
        return geographyPersistencePort.findWardByCode(code);
    }

    /**
     * Resolve complete location with parent-child validation using a SINGLE
     * database query.
     * Uses JOIN FETCH to load ward, district, and province in one roundtrip.
     * Validates that district belongs to province and ward belongs to district.
     * Uses caching to avoid repeated queries for static geography data.
     * 
     * @param provinceCode the province code
     * @param districtCode the district code
     * @param wardCode     the ward code
     * @return the resolved location with validated hierarchy
     * @throws com.ecommerce.identity.domain.exception.IdentityException if any code
     *                                                                   is invalid
     *                                                                   or
     *                                                                   hierarchy
     *                                                                   is
     *                                                                   incorrect
     */
    public ResolvedLocation resolveLocation(String provinceCode, String districtCode, String wardCode) {
        log.debug("Resolving location: province={}, district={}, ward={}", provinceCode, districtCode, wardCode);
        return geographyPersistencePort.resolveLocationWithValidation(provinceCode, districtCode, wardCode);
    }
}
