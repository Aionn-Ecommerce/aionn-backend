package com.aionn.identity.adapter.rest.controller;

import com.aionn.identity.adapter.rest.dto.geography.response.GeographyResponse;
import com.aionn.identity.adapter.rest.exception.IdentityExceptionHandler;
import com.aionn.identity.adapter.rest.mapper.geography.GeographyDtoMapper;
import com.aionn.identity.application.dto.geography.result.GeographyResult;
import com.aionn.identity.application.service.GeographyService;
import com.aionn.sharedkernel.adapter.web.support.clientip.ClientIpArgumentResolver;
import com.aionn.sharedkernel.infrastructure.web.ClientIpResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web tests for GeographyController. Verifies the public reference data
 * endpoints
 * (countries, provinces, districts, wards) return the expected payload, support
 * optional filters, and bubble service-layer errors through the exception
 * handler.
 */
@ExtendWith(MockitoExtension.class)
class GeographyControllerWebTest {

    @Mock
    private GeographyService geographyService;
    @Mock
    private GeographyDtoMapper geographyDtoMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        GeographyController controller = new GeographyController(geographyService, geographyDtoMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new IdentityExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        Jackson2ObjectMapperBuilder.json().build()))
                .setCustomArgumentResolvers(new ClientIpArgumentResolver(new ClientIpResolver()))
                .build();
    }

    @Test
    void listCountriesReturnsAllActiveCountries() throws Exception {
        List<GeographyResult> results = List.of(
                new GeographyResult("VN", "Việt Nam", "Vietnam"),
                new GeographyResult("US", "Hoa Kỳ", "United States"));
        List<GeographyResponse> responses = List.of(
                new GeographyResponse("VN", "Việt Nam", "Vietnam"),
                new GeographyResponse("US", "Hoa Kỳ", "United States"));

        when(geographyService.listCountries()).thenReturn(results);
        when(geographyDtoMapper.toResponses(results)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/geography/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].code").value("VN"))
                .andExpect(jsonPath("$.data[0].name").value("Việt Nam"))
                .andExpect(jsonPath("$.data[1].code").value("US"));

        verify(geographyService).listCountries();
    }

    @Test
    void getCountryReturnsCountryByCode() throws Exception {
        GeographyResult result = new GeographyResult("VN", "Việt Nam", "Vietnam");
        GeographyResponse response = new GeographyResponse("VN", "Việt Nam", "Vietnam");

        when(geographyService.getCountry("VN")).thenReturn(result);
        when(geographyDtoMapper.toResponse(result)).thenReturn(response);

        mockMvc.perform(get("/api/v1/geography/countries/VN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("VN"))
                .andExpect(jsonPath("$.data.name").value("Việt Nam"))
                .andExpect(jsonPath("$.data.nameEn").value("Vietnam"));

        verify(geographyService).getCountry("VN");
    }

    @Test
    void listProvincesWithoutCountryFilterReturnsAllProvinces() throws Exception {
        List<GeographyResult> results = List.of(
                new GeographyResult("01", "Hà Nội", "Hanoi"),
                new GeographyResult("79", "Hồ Chí Minh", "Ho Chi Minh"));
        List<GeographyResponse> responses = List.of(
                new GeographyResponse("01", "Hà Nội", "Hanoi"),
                new GeographyResponse("79", "Hồ Chí Minh", "Ho Chi Minh"));

        when(geographyService.listProvinces(null)).thenReturn(results);
        when(geographyDtoMapper.toResponses(results)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/geography/provinces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].code").value("01"))
                .andExpect(jsonPath("$.data[1].code").value("79"));

        verify(geographyService).listProvinces(null);
    }

    @Test
    void listProvincesWithCountryFilterPassesCountryCodeToService() throws Exception {
        List<GeographyResult> results = List.of(new GeographyResult("01", "Hà Nội", "Hanoi"));
        List<GeographyResponse> responses = List.of(new GeographyResponse("01", "Hà Nội", "Hanoi"));

        when(geographyService.listProvinces("VN")).thenReturn(results);
        when(geographyDtoMapper.toResponses(results)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/geography/provinces").param("countryCode", "VN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("01"));

        verify(geographyService).listProvinces("VN");
    }

    @Test
    void getProvinceReturnsProvinceByCode() throws Exception {
        GeographyResult result = new GeographyResult("01", "Hà Nội", "Hanoi");
        GeographyResponse response = new GeographyResponse("01", "Hà Nội", "Hanoi");

        when(geographyService.getProvince("01")).thenReturn(result);
        when(geographyDtoMapper.toResponse(result)).thenReturn(response);

        mockMvc.perform(get("/api/v1/geography/provinces/01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("01"))
                .andExpect(jsonPath("$.data.name").value("Hà Nội"));

        verify(geographyService).getProvince("01");
    }

    @Test
    void listDistrictsRequiresProvinceCode() throws Exception {
        // Without the required `provinceCode` param, MockMvc resolves to a 400.
        mockMvc.perform(get("/api/v1/geography/districts"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(geographyService);
    }

    @Test
    void listDistrictsByProvinceCodeReturnsFilteredDistricts() throws Exception {
        List<GeographyResult> results = List.of(
                new GeographyResult("001", "Ba Đình", "Ba Dinh"),
                new GeographyResult("002", "Hoàn Kiếm", "Hoan Kiem"));
        List<GeographyResponse> responses = List.of(
                new GeographyResponse("001", "Ba Đình", "Ba Dinh"),
                new GeographyResponse("002", "Hoàn Kiếm", "Hoan Kiem"));

        when(geographyService.listDistricts("01")).thenReturn(results);
        when(geographyDtoMapper.toResponses(results)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/geography/districts").param("provinceCode", "01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].code").value("001"))
                .andExpect(jsonPath("$.data[1].name").value("Hoàn Kiếm"));

        verify(geographyService).listDistricts("01");
    }

    @Test
    void getDistrictReturnsDistrictByCode() throws Exception {
        GeographyResult result = new GeographyResult("001", "Ba Đình", "Ba Dinh");
        GeographyResponse response = new GeographyResponse("001", "Ba Đình", "Ba Dinh");

        when(geographyService.getDistrict("001")).thenReturn(result);
        when(geographyDtoMapper.toResponse(result)).thenReturn(response);

        mockMvc.perform(get("/api/v1/geography/districts/001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("001"))
                .andExpect(jsonPath("$.data.name").value("Ba Đình"));

        verify(geographyService).getDistrict("001");
    }

    @Test
    void listWardsRequiresDistrictCode() throws Exception {
        mockMvc.perform(get("/api/v1/geography/wards"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(geographyService);
    }

    @Test
    void listWardsByDistrictCodeReturnsFilteredWards() throws Exception {
        List<GeographyResult> results = List.of(
                new GeographyResult("00001", "Phúc Xá", "Phuc Xa"),
                new GeographyResult("00004", "Trúc Bạch", "Truc Bach"));
        List<GeographyResponse> responses = List.of(
                new GeographyResponse("00001", "Phúc Xá", "Phuc Xa"),
                new GeographyResponse("00004", "Trúc Bạch", "Truc Bach"));

        when(geographyService.listWards("001")).thenReturn(results);
        when(geographyDtoMapper.toResponses(results)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/geography/wards").param("districtCode", "001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].code").value("00001"))
                .andExpect(jsonPath("$.data[1].nameEn").value("Truc Bach"));

        verify(geographyService).listWards("001");
    }

    @Test
    void getWardReturnsWardByCode() throws Exception {
        GeographyResult result = new GeographyResult("00001", "Phúc Xá", "Phuc Xa");
        GeographyResponse response = new GeographyResponse("00001", "Phúc Xá", "Phuc Xa");

        when(geographyService.getWard("00001")).thenReturn(result);
        when(geographyDtoMapper.toResponse(result)).thenReturn(response);

        mockMvc.perform(get("/api/v1/geography/wards/00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("00001"))
                .andExpect(jsonPath("$.data.name").value("Phúc Xá"));

        verify(geographyService).getWard("00001");
    }
}
