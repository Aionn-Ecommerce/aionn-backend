package com.aionn.identity.domain.geography;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "countries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Country {

    @Id
    @Column(length = 2)
    private String code; // VN, US, JP

    @Column(nullable = false, length = 100)
    private String name; // ViÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡t Nam, United States

    @Column(name = "name_en", length = 100)
    private String nameEn; // Vietnam, United States

    @Column(name = "phone_code", length = 10)
    private String phoneCode; // +84, +1

    @Column(nullable = false)
    private Boolean active = true;
}



