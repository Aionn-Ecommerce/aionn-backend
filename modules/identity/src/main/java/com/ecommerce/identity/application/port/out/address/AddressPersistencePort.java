package com.ecommerce.identity.application.port.out.address;

import com.ecommerce.identity.domain.model.Address;

import java.util.List;
import java.util.Optional;

public interface AddressPersistencePort {

    List<Address> findByUserId(String userId);

    long countByUserId(String userId);

    Address save(Address address);

    Optional<Address> findByAddressIdAndUserId(String addressId, String userId);

    void clearDefaultByUserId(String userId);

    void delete(Address address);
}
