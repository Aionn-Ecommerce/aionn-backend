package com.aionn.catalog.domain.model;

import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.valueobject.BrandStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandTest {

    private static final String BRAND_ID = "01HZBRD0000000000000000001";

    @Test
    void createInitializesAsActive() {
        Brand brand = Brand.create(BRAND_ID, "Acme", null, "desc");

        assertThat(brand.getStatus()).isEqualTo(BrandStatus.ACTIVE);
        assertThat(brand.pullEvents()).hasSize(1);
    }

    @Test
    void createRejectsBlankName() {
        assertThatThrownBy(() -> Brand.create(BRAND_ID, " ", null, null))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.INVALID_ARGUMENT.getCode());
    }

    @Test
    void softDeleteSetsDeletedStatus() {
        Brand brand = Brand.create(BRAND_ID, "Acme", null, null);
        brand.pullEvents();

        brand.softDelete("policy");

        assertThat(brand.getStatus()).isEqualTo(BrandStatus.DELETED);
        assertThat(brand.pullEvents()).hasSize(1);
    }

    @Test
    void updateOnDeletedBrandThrows() {
        Brand brand = Brand.create(BRAND_ID, "Acme", null, null);
        brand.softDelete("policy");

        assertThatThrownBy(() -> brand.update("Renamed", null, null))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.BRAND_NOT_FOUND.getCode());
    }
}
