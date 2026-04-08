package com.ecommerce.identity.domain.geography;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provinces", indexes = {
        @Index(name = "idx_province_country", columnList = "country_code")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Province {

    @Id
    @Column(length = 10)
    private String code; // VN-HN, VN-SG

    @Column(nullable = false, length = 100)
    private String name; // HÃƒÂ  NÃ¡Â»â„¢i, TP. HÃ¡Â»â€œ ChÃƒÂ­ Minh

    @Column(name = "name_en", length = 100)
    private String nameEn; // Hanoi, Ho Chi Minh City

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(nullable = false)
    private Boolean active = true;
}


