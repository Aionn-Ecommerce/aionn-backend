package com.ecommerce.identity.domain.geography;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wards", indexes = {
        @Index(name = "idx_ward_district", columnList = "district_code")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Ward {

    @Id
    @Column(length = 15)
    private String code; // VN-HN-BA-PH, VN-SG-Q1-BN

    @Column(nullable = false, length = 100)
    private String name; // Phúc Xá, Bến Nghé

    @Column(name = "name_en", length = 100)
    private String nameEn; // Phuc Xa, Ben Nghe

    @Column(name = "district_code", nullable = false, length = 10, insertable = false, updatable = false)
    private String districtCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_code", nullable = false)
    private District district;

    @Column(nullable = false)
    private Boolean active = true;
}
