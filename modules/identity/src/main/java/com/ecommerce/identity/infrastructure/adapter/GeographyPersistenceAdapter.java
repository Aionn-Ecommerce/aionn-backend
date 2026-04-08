package com.ecommerce.identity.infrastructure.adapter;

import com.ecommerce.identity.application.dto.geography.result.GeographyResult;
import com.ecommerce.identity.application.dto.geography.result.ResolvedLocation;
import com.ecommerce.identity.application.port.out.geography.GeographyPersistencePort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.geography.District;
import com.ecommerce.identity.domain.geography.Province;
import com.ecommerce.identity.domain.geography.Ward;
import com.ecommerce.identity.infrastructure.persistence.repository.geography.CountryRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.geography.DistrictRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.geography.ProvinceRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.geography.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GeographyPersistenceAdapter implements GeographyPersistencePort {

    private final CountryRepository countryRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;

    @Override
    public List<GeographyResult> findAllCountries() {
        return countryRepository.findByActiveTrue().stream()
                .map(c -> new GeographyResult(c.getCode(), c.getName(), c.getNameEn()))
                .toList();
    }

    @Override
    @Cacheable(value = "geography", key = "'country:' + #code")
    public Optional<GeographyResult> findCountryByCode(String code) {
        return countryRepository.findByCodeAndActiveTrue(code)
                .map(c -> new GeographyResult(c.getCode(), c.getName(), c.getNameEn()));
    }

    @Override
    public List<GeographyResult> findProvincesByCountryCode(String countryCode) {
        return provinceRepository.findByCountryCodeAndActiveTrue(countryCode).stream()
                .map(p -> new GeographyResult(p.getCode(), p.getName(), p.getNameEn()))
                .toList();
    }

    @Override
    public List<GeographyResult> findAllProvinces() {
        return provinceRepository.findByActiveTrue().stream()
                .map(p -> new GeographyResult(p.getCode(), p.getName(), p.getNameEn()))
                .toList();
    }

    @Override
    @Cacheable(value = "geography", key = "'province:' + #code")
    public Optional<GeographyResult> findProvinceByCode(String code) {
        return provinceRepository.findByCodeAndActiveTrue(code)
                .map(p -> new GeographyResult(p.getCode(), p.getName(), p.getNameEn()));
    }

    @Override
    public List<GeographyResult> findDistrictsByProvinceCode(String provinceCode) {
        return districtRepository.findByProvinceCodeAndActiveTrue(provinceCode).stream()
                .map(d -> new GeographyResult(d.getCode(), d.getName(), d.getNameEn()))
                .toList();
    }

    @Override
    @Cacheable(value = "geography", key = "'district:' + #code")
    public Optional<GeographyResult> findDistrictByCode(String code) {
        return districtRepository.findByCodeAndActiveTrue(code)
                .map(d -> new GeographyResult(d.getCode(), d.getName(), d.getNameEn()));
    }

    @Override
    public List<GeographyResult> findWardsByDistrictCode(String districtCode) {
        return wardRepository.findByDistrictCodeAndActiveTrue(districtCode).stream()
                .map(w -> new GeographyResult(w.getCode(), w.getName(), w.getNameEn()))
                .toList();
    }

    @Override
    @Cacheable(value = "geography", key = "'ward:' + #code")
    public Optional<GeographyResult> findWardByCode(String code) {
        return wardRepository.findByCodeAndActiveTrue(code)
                .map(w -> new GeographyResult(w.getCode(), w.getName(), w.getNameEn()));
    }

    @Override
    public ResolvedLocation resolveLocationWithValidation(String provinceCode, String districtCode, String wardCode) {
        Ward ward = wardRepository.findByCodeWithDistrictAndProvince(wardCode)
                .orElseThrow(() -> new IdentityException(
                        IdentityErrorCode.INVALID_GEOGRAPHY_CODE,
                        "Invalid ward code: " + wardCode));

        District district = ward.getDistrict();
        Province province = district.getProvince();

        if (!ward.getDistrictCode().equals(districtCode)) {
            throw new IdentityException(
                    IdentityErrorCode.INVALID_GEOGRAPHY_CODE,
                    "Ward " + wardCode + " does not belong to district " + districtCode);
        }

        if (!district.getProvinceCode().equals(provinceCode)) {
            throw new IdentityException(
                    IdentityErrorCode.INVALID_GEOGRAPHY_CODE,
                    "District " + districtCode + " does not belong to province " + provinceCode);
        }

        return new ResolvedLocation(
                new GeographyResult(province.getCode(), province.getName(), province.getNameEn()),
                new GeographyResult(district.getCode(), district.getName(), district.getNameEn()),
                new GeographyResult(ward.getCode(), ward.getName(), ward.getNameEn()));
    }
}
