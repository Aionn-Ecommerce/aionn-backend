package com.ecommerce.identity.domain.geography;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "districts", indexes = {
        @Index(name = "idx_district_province", columnList = "province_code")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class District {

    @Id
    @Column(length = 10)
    private String code; // VN-HN-BA, VN-SG-Q1

    @Column(nullable = false, length = 100)
    private String name; // Ba Đình, Quận 1

    @Column(name = "name_en", length = 100)
    private String nameEn; // Ba Dinh, District 1

    @Column(name = "province_code", nullable = false, length = 10)
    private String provinceCode;

    @Column(nullable = false)
    private Boolean active = true;
}
