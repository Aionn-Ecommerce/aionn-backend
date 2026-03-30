package com.ecommerce.identity.application.port.in.address;

import com.ecommerce.identity.application.dto.address.AddressResult;
import java.util.List;

public interface ListAddressesQueryPort {
    List<AddressResult> execute(String userId);
}