package com.aionn.identity.infrastructure.integration;

import com.aionn.identity.application.dto.geography.result.ResolvedLocation;
import com.aionn.identity.application.service.GeographyService;
import com.aionn.sharedkernel.integration.port.identity.AddressLookupPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityAddressLookupAdapter implements AddressLookupPort {

    private final GeographyService geographyService;

    @Override
    public Optional<ResolvedAddress> resolve(String provinceCode, String districtCode, String wardCode) {
        if (provinceCode == null || districtCode == null || wardCode == null) {
            return Optional.empty();
        }
        try {
            ResolvedLocation loc = geographyService.resolveLocation(provinceCode, districtCode, wardCode);
            return Optional.of(new ResolvedAddress(
                    loc.province().code(), loc.province().name(),
                    loc.district().code(), loc.district().name(),
                    loc.ward().code(), loc.ward().name()));
        } catch (RuntimeException ex) {
            log.warn("AddressLookup failed for province={} district={} ward={}: {}",
                    provinceCode, districtCode, wardCode, ex.getMessage());
            return Optional.empty();
        }
    }
}
