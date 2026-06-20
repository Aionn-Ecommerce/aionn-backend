package com.aionn.catalog.application.service;

import com.aionn.catalog.application.dto.brand.command.CreateBrandCommand;
import com.aionn.catalog.application.dto.brand.command.DeleteBrandCommand;
import com.aionn.catalog.application.dto.brand.command.UpdateBrandCommand;
import com.aionn.catalog.application.dto.brand.result.BrandResult;
import com.aionn.catalog.application.mapper.BrandResultMapper;
import com.aionn.catalog.application.port.out.BrandPersistencePort;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.model.Brand;
import com.aionn.catalog.domain.valueobject.BrandStatus;
import com.aionn.sharedkernel.application.port.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandPersistencePort brandRepository;
    @Mock
    private BrandResultMapper brandResultMapper;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private BrandService brandService;

    private BrandResult sampleResult;

    @BeforeEach
    void setUp() {
        sampleResult = new BrandResult(
                "01HZBRD0000000000000000001",
                "Acme",
                null,
                "desc",
                BrandStatus.ACTIVE.name(),
                Instant.now(),
                Instant.now());
    }

    @Test
    void createPersistsAndPublishesEvents() {
        when(brandRepository.existsByName("Acme")).thenReturn(false);
        when(brandRepository.save(any(Brand.class))).thenAnswer(inv -> inv.getArgument(0));
        when(brandResultMapper.toResult(any(Brand.class))).thenReturn(sampleResult);

        BrandResult result = brandService.create(new CreateBrandCommand("Acme", null, "desc"));

        assertThat(result).isEqualTo(sampleResult);
        ArgumentCaptor<Brand> captor = ArgumentCaptor.forClass(Brand.class);
        verify(brandRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Acme");
        verify(eventPublisher).publish(anyCollection());
    }

    @Test
    void createThrowsWhenNameAlreadyExists() {
        when(brandRepository.existsByName("Acme")).thenReturn(true);

        assertThatThrownBy(() -> brandService.create(new CreateBrandCommand("Acme", null, null)))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.BRAND_NAME_CONFLICT.getCode());

        verify(brandRepository, never()).save(any());
    }

    @Test
    void deleteRejectsBrandWithActiveProducts() {
        Brand brand = Brand.create("01HZBRD0000000000000000001", "Acme", null, null);
        brand.pullEvents();
        when(brandRepository.findById("01HZBRD0000000000000000001")).thenReturn(Optional.of(brand));
        when(brandRepository.hasActiveProducts("01HZBRD0000000000000000001")).thenReturn(true);

        assertThatThrownBy(() -> brandService.delete(new DeleteBrandCommand("01HZBRD0000000000000000001", "stop")))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.BRAND_HAS_ACTIVE_PRODUCTS.getCode());
    }

    @Test
    void getReturnsResultWhenBrandFound() {
        Brand brand = Brand.create("01HZBRD0000000000000000001", "Acme", null, null);
        when(brandRepository.findById("01HZBRD0000000000000000001")).thenReturn(Optional.of(brand));
        when(brandResultMapper.toResult(brand)).thenReturn(sampleResult);

        BrandResult result = brandService.get("01HZBRD0000000000000000001");

        assertThat(result).isEqualTo(sampleResult);
    }

    @Test
    void updateThrowsWhenBrandNotFound() {
        when(brandRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> brandService.update(new UpdateBrandCommand("missing", "X", null, null)))
                .isInstanceOf(CatalogException.class)
                .extracting("errorCode")
                .isEqualTo(CatalogErrorCode.BRAND_NOT_FOUND.getCode());
    }
}
