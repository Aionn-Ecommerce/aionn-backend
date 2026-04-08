package com.ecommerce.identity.infrastructure.policy;

import com.ecommerce.identity.application.port.out.address.AddressPolicy;
import com.ecommerce.identity.infrastructure.config.properties.AddressProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAddressPolicy implements AddressPolicy {

    private final AddressProperties addressProperties;

    @Override
    public long getMaxAddressNumbers() {
        return addressProperties.maxAddressNumbers();
    }
}
