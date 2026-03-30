package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.geography.GeographyResult;
import com.ecommerce.identity.domain.geography.Country;
import com.ecommerce.identity.domain.geography.District;
import com.ecommerce.identity.domain.geography.Province;
import com.ecommerce.identity.domain.geography.Ward;
import com.ecommerce.identity.infrastructure.persistence.geography.CountryRepository;
import com.ecommerce.identity.infrastructure.persistence.geography.DistrictRepository;
import com.ecommerce.identity.infrastructure.persistence.geography.ProvinceRepository;
import com.ecommerce.identity.infrastructure.persistence.geography.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GeographyService {

    private final CountryRepository countryRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;

    @Transactional(readOnly = true)
    public List<GeographyResult> listCountries() {
        return countryRepository.findByActiveTrue().stream()
                .map(this::toResult)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<GeographyResult> getCountry(String code) {
        return countryRepository.findByCodeAndActiveTrue(code)
                .map(this::toResult);
    }

    @Transactional(readOnly = true)
    public List<GeographyResult> listProvinces(String countryCode) {
        if (countryCode != null) {
            return provinceRepository.findByCountryCodeAndActiveTrue(countryCode).stream()
                    .map(this::toResult)
                    .toList();
        }
        return provinceRepository.findByActiveTrue().stream()
                .map(this::toResult)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<GeographyResult> getProvince(String code) {
        return provinceRepository.findByCodeAndActiveTrue(code)
                .map(this::toResult);
    }

    @Transactional(readOnly = true)
    public List<GeographyResult> listDistricts(String provinceCode) {
        return districtRepository.findByProvinceCodeAndActiveTrue(provinceCode).stream()
                .map(this::toResult)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<GeographyResult> getDistrict(String code) {
        return districtRepository.findByCodeAndActiveTrue(code)
                .map(this::toResult);
    }

    @Transactional(readOnly = true)
    public List<GeographyResult> listWards(String districtCode) {
        return wardRepository.findByDistrictCodeAndActiveTrue(districtCode).stream()
                .map(this::toResult)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<GeographyResult> getWard(String code) {
        return wardRepository.findByCodeAndActiveTrue(code)
                .map(this::toResult);
    }

    private GeographyResult toResult(Country country) {
        return new GeographyResult(country.getCode(), country.getName(), country.getNameEn());
    }

    private GeographyResult toResult(Province province) {
        return new GeographyResult(province.getCode(), province.getName(), province.getNameEn());
    }

    private GeographyResult toResult(District district) {
        return new GeographyResult(district.getCode(), district.getName(), district.getNameEn());
    }

    private GeographyResult toResult(Ward ward) {
        return new GeographyResult(ward.getCode(), ward.getName(), ward.getNameEn());
    }
}
