package com.ecommerce.sharedkernel.domain.vo;

import com.ecommerce.sharedkernel.domain.model.ValueObject;

public record Address(
        String street,
        String ward,
        String district,
        String city,
        String country,
        String zipCode) implements ValueObject {

    public Address {
        street = requireNonBlank(street, "street");
        city = requireNonBlank(city, "city");
        country = requireNonBlank(country, "country");

        if (country.length() != 2) {
            throw new IllegalArgumentException("Country must be ISO 3166-1 alpha-2: " + country);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Address of(String street, String city, String country) {
        return builder().street(street).city(city).country(country).build();
    }

    public String toFullString() {
        StringBuilder sb = new StringBuilder(street);
        if (ward != null && !ward.isBlank())
            sb.append(", ").append(ward);
        if (district != null && !district.isBlank())
            sb.append(", ").append(district);
        sb.append(", ").append(city);
        sb.append(", ").append(country);
        if (zipCode != null && !zipCode.isBlank())
            sb.append(" ").append(zipCode);
        return sb.toString();
    }

    @Override
    public String toString() {
        return toFullString();
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Address." + fieldName + " must not be blank");
        }
        return value.trim();
    }

    public static final class Builder {
        private String street;
        private String ward;
        private String district;
        private String city;
        private String country = "VN";
        private String zipCode;

        private Builder() {
        }

        public Builder street(String street) {
            this.street = street;
            return this;
        }

        public Builder ward(String ward) {
            this.ward = ward;
            return this;
        }

        public Builder district(String district) {
            this.district = district;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder zipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }

        public Address build() {
            return new Address(street, ward, district, city, country, zipCode);
        }
    }
}