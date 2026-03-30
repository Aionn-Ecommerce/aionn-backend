package com.ecommerce.identity.domain.model;

import com.ecommerce.identity.domain.valueobject.AddressType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder(toBuilder = true)
public class Address {

    private final String addressId;
    private final String userId;
    private final String contactName;
    private final String phone;
    private final String provinceCode;
    private final String provinceName;
    private final String districtCode;
    private final String districtName;
    private final String wardCode;
    private final String wardName;
    private final String detailAddress;
    private final String fullAddress;
    private final AddressType type;
    private final boolean isDefault;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public boolean canBeDeleted() {
        return !isDefault;
    }
}
