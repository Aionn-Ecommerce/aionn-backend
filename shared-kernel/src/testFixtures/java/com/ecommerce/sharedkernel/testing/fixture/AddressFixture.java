package com.ecommerce.sharedkernel.testing.fixture;

import com.ecommerce.sharedkernel.domain.vo.Address;

public final class AddressFixture {

    private AddressFixture() {
    }

    public static Address hoChiMinh() {
        return Address.builder()
                .street("123 Nguyễn Huệ")
                .ward("Bến Nghé")
                .district("Quận 1")
                .city("Hồ Chí Minh")
                .country("VN")
                .zipCode("700000")
                .build();
    }

    public static Address hanoi() {
        return Address.builder()
                .street("01 Hoàn Kiếm")
                .ward("Hàng Trống")
                .district("Hoàn Kiếm")
                .city("Hà Nội")
                .country("VN")
                .zipCode("100000")
                .build();
    }

    public static Address danang() {
        return Address.builder()
                .street("74 Bạch Đằng")
                .district("Hải Châu")
                .city("Đà Nẵng")
                .country("VN")
                .build();
    }

    public static Address international() {
        return Address.builder()
                .street("1 Infinite Loop")
                .city("Cupertino")
                .country("US")
                .zipCode("95014")
                .build();
    }

    /**
     * Create an address with a custom street (all other fields are Ho Chi Minh
     * defaults)
     */
    public static Address withStreet(String street) {
        return Address.builder()
                .street(street)
                .ward("Bến Nghé")
                .district("Quận 1")
                .city("Hồ Chí Minh")
                .country("VN")
                .build();
    }
}
