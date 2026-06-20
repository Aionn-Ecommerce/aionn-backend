package com.aionn.shipping.application.service;

import com.aionn.shipping.application.dto.rate.result.ShippingQuoteResult;
import com.aionn.shipping.application.dto.shipment.command.CarrierWebhookCommand;
import com.aionn.shipping.application.dto.shipment.command.CreateShipmentCommand;
import com.aionn.shipping.application.dto.shipment.command.QuoteShippingCommand;
import com.aionn.shipping.application.mapper.ShippingResultMapper;
import com.aionn.shipping.application.port.out.CarrierClient;
import com.aionn.shipping.application.port.out.ShipmentPersistencePort;
import com.aionn.shipping.application.port.out.ShippingRatePersistencePort;
import com.aionn.shipping.application.port.out.integration.ShippingIntegrationEventPublisherPort;
import com.aionn.shipping.domain.exception.ShippingErrorCode;
import com.aionn.shipping.domain.exception.ShippingException;
import com.aionn.shipping.domain.model.Shipment;
import com.aionn.shipping.domain.model.ShippingRate;
import com.aionn.shipping.domain.valueobject.ShipmentAddress;
import com.aionn.shipping.domain.valueobject.ShipmentDimensions;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.integration.port.catalog.MerchantQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    ShipmentPersistencePort shipmentRepository;
    @Mock
    ShippingRatePersistencePort rateRepository;
    @Mock
    ShippingResultMapper mapper;
    @Mock
    EventPublisher eventPublisher;
    @Mock
    CarrierClient carrierClient;
    @Mock
    ShippingIntegrationEventPublisherPort integrationEventPublisher;
    @Mock
    MerchantQueryPort merchantQueryPort;

    ShipmentService service;

    private static final ShipmentAddress ADDRESS = new ShipmentAddress(
            "John Doe", "0912345678", "123 Main", "00001", "001", "HN", "VN");
    private static final ShipmentDimensions DIMENSIONS = new ShipmentDimensions(
            500, BigDecimal.valueOf(20), BigDecimal.valueOf(15), BigDecimal.valueOf(10));

    @BeforeEach
    void setUp() {
        // self can be the same instance for test purposes; the public entrypoints
        // we call below do not exercise the @Lazy self-invocation contract.
        service = new ShipmentService(shipmentRepository, rateRepository, mapper, eventPublisher,
                carrierClient, integrationEventPublisher, merchantQueryPort, null);
    }

    @Test
    void createShipmentSavesAndPublishesEvents() {
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createShipment(new CreateShipmentCommand("ORDER_1", "M_1", "U_1",
                ADDRESS, DIMENSIONS, BigDecimal.ZERO, BigDecimal.valueOf(30000), "VND"));

        verify(shipmentRepository).save(any(Shipment.class));
        verify(eventPublisher).publish(anyCollection());
    }

    @Test
    void quoteReturnsConfiguredRateWhenAvailable() {
        ShippingRate rate = ShippingRate.configure("R_1", "HN",
                BigDecimal.valueOf(25000), "VND", "<=2kg");
        when(rateRepository.findByZoneCode("HN")).thenReturn(Optional.of(rate));

        ShippingQuoteResult result = service.quote(new QuoteShippingCommand(
                "ORDER_1", ADDRESS, DIMENSIONS, "VND"));

        assertThat(result.fee()).isEqualByComparingTo(BigDecimal.valueOf(25000));
        assertThat(result.source()).isEqualTo("configured-rate");
        assertThat(result.zoneCode()).isEqualTo("HN");
    }

    @Test
    void quoteFallsBackToCarrierWhenNoRateConfigured() {
        when(rateRepository.findByZoneCode("HN")).thenReturn(Optional.empty());
        CarrierClient.Quote carrierQuote = new CarrierClient.Quote(
                BigDecimal.valueOf(40000), "VND", "HN", "carrier-detail",
                Instant.now().plusSeconds(86400), Instant.now());
        when(carrierClient.quote(eq(ADDRESS), eq(DIMENSIONS), eq("VND")))
                .thenReturn(carrierQuote);

        ShippingQuoteResult result = service.quote(new QuoteShippingCommand(
                "ORDER_1", ADDRESS, DIMENSIONS, "VND"));

        assertThat(result.fee()).isEqualByComparingTo(BigDecimal.valueOf(40000));
        assertThat(result.source()).isEqualTo("carrier");
    }

    @Test
    void applyCarrierWebhookThrowsWhenShipmentNotFound() {
        when(shipmentRepository.findByTrackingCode("UNKNOWN_TRACK"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyCarrierWebhook(new CarrierWebhookCommand(
                "UNKNOWN_TRACK", "PICKED_UP", null, null, null, null, null, null, "WH_1")))
                .isInstanceOf(ShippingException.class)
                .extracting("errorCode")
                .isEqualTo(ShippingErrorCode.SHIPMENT_NOT_FOUND.getCode());
    }

    @Test
    void applyCarrierWebhookForDeliveredPublishesIntegrationEvent() {
        Shipment shipment = Shipment.request("S_1", "ORDER_1", "M_1", "U_1",
                ADDRESS, DIMENSIONS, BigDecimal.ZERO, BigDecimal.valueOf(30000), "VND");
        shipment.registerWithCarrier("TRACK_1", "CARRIER_1", null);
        shipment.markPickedUp("WH_1");
        shipment.markOutForDelivery("Driver", "0901111222");
        shipment.pullEvents();
        when(shipmentRepository.findByTrackingCode("TRACK_1")).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        service.applyCarrierWebhook(new CarrierWebhookCommand(
                "TRACK_1", "DELIVERED", null, null, null, null, "https://sig", null, null));

        verify(integrationEventPublisher).publishDelivered(
                eq("S_1"), eq("ORDER_1"), eq("https://sig"), any());
    }

    @Test
    void applyCarrierWebhookRejectsUnknownType() {
        Shipment shipment = Shipment.request("S_1", "ORDER_1", "M_1", "U_1",
                ADDRESS, DIMENSIONS, BigDecimal.ZERO, BigDecimal.valueOf(30000), "VND");
        when(shipmentRepository.findByTrackingCode("TRACK_1")).thenReturn(Optional.of(shipment));

        assertThatThrownBy(() -> service.applyCarrierWebhook(new CarrierWebhookCommand(
                "TRACK_1", "UNKNOWN", null, null, null, null, null, null, null)))
                .isInstanceOf(ShippingException.class)
                .extracting("errorCode")
                .isEqualTo(ShippingErrorCode.INVALID_ARGUMENT.getCode());
    }
}
