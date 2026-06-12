package com.aionn.sharedkernel.integration.port.identity;

import java.util.Optional;

public interface AddressLookupPort {

    Optional<ResolvedAddress> resolve(String provinceCode, String districtCode, String wardCode);

    record ResolvedAddress(
            String provinceCode,
            String provinceName,
            String districtCode,
            String districtName,
            String wardCode,
            String wardName) {
    }
}
