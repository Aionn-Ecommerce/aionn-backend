package com.aionn.shipping.application.service;

import com.aionn.shipping.application.dto.rate.result.ShippingQuoteResult;
import com.aionn.shipping.application.dto.shipment.command.CancelShipmentCommand;
import com.aionn.shipping.application.dto.shipment.command.CarrierWebhookCommand;
import com.aionn.shipping.application.dto.shipment.command.CreateShipmentCommand;
import com.aionn.shipping.application.dto.shipment.command.FetchLabelCommand;
import com.aionn.shipping.application.dto.shipment.command.QuoteShippingCommand;
import com.aionn.shipping.application.dto.shipment.command.ResolveIssueCommand;
import com.aionn.shipping.application.dto.shipment.result.ShipmentResult;
import com.aionn.shipping.application.mapper.ShippingResultMapper;
import com.aionn.shipping.application.port.out.CarrierClient;
import com.aionn.shipping.application.port.out.ShipmentPersistencePort;
import com.aionn.shipping.application.port.out.ShippingRatePersistencePort;
import com.aionn.shipping.application.port.out.integration.ShippingIntegrationEventPublisherPort;
import com.aionn.shipping.domain.exception.ShippingErrorCode;
import com.aionn.shipping.domain.exception.ShippingException;
import com.aionn.shipping.domain.model.Shipment;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.integration.port.catalog.MerchantQueryPort;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ShipmentService {

    private final ShipmentPersistencePort shipmentRepository;
    private final ShippingRatePersistencePort rateRepository;
    private final ShippingResultMapper mapper;
    private final EventPublisher eventPublisher;
    private final CarrierClient carrierClient;
    private final ShippingIntegrationEventPublisherPort integrationEventPublisher;
    private final MerchantQueryPort merchantQueryPort;
    private final ShipmentService self;

    public ShipmentService(ShipmentPersistencePort shipmentRepository,
            ShippingRatePersistencePort rateRepository,
            ShippingResultMapper mapper,
            EventPublisher eventPublisher,
            CarrierClient carrierClient,
            ShippingIntegrationEventPublisherPort integrationEventPublisher,
            MerchantQueryPort merchantQueryPort,
            @Autowired @Lazy ShipmentService self) {
        this.shipmentRepository = shipmentRepository;
        this.rateRepository = rateRepository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
        this.carrierClient = carrierClient;
        this.integrationEventPublisher = integrationEventPublisher;
        this.merchantQueryPort = merchantQueryPort;
        this.self = self;
    }

    @Transactional
    public ShipmentResult createShipment(CreateShipmentCommand command) {
        Shipment shipment = Shipment.request(IdGenerator.ulid(), command.orderId(),
                command.merchantId(), command.userId(),
                command.address(), command.dimensions(), command.codAmount(),
                command.shippingFee(), command.currency());
        Shipment saved = shipmentRepository.save(shipment);
        eventPublisher.publish(shipment.pullEvents());
        return mapper.toResult(saved);
    }

    /**
     * Atomic from the caller's perspective: create the shipment record locally
     * then immediately register it with the carrier. The carrier I/O is done
     * outside the DB transaction so the connection isn't held while waiting on
     * GHN.
     */
    public ShipmentResult createAndRegister(CreateShipmentCommand command) {
        ShipmentResult created = self.createShipment(command);
        try {
            return registerWithCarrier(created.shipmentId());
        } catch (RuntimeException ex) {
            try {
                self.applyCancel(created.shipmentId(), "carrier-registration-failed");
            } catch (RuntimeException compensateEx) {
                log.error("Failed to compensate orphan shipment {} after carrier registration failed",
                        created.shipmentId(), compensateEx);
            }
            throw ex;
        }
    }

    public ShipmentResult registerWithCarrier(String shipmentId) {
        Shipment shipment = self.loadShipment(shipmentId);
        if (shipment.getTrackingCode() != null) {
            return mapper.toResult(shipment);
        }
        CarrierClient.Registration reg;
        try {
            reg = carrierClient.register(
                    shipment.getShipmentId(),
                    shipment.getOrderId(),
                    shipment.getAddress(),
                    shipment.getDimensions(),
                    shipment.getCodAmount(),
                    shipment.getShippingFee(),
                    shipment.getCurrency());
        } catch (ShippingException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ShippingException(ShippingErrorCode.SHIPMENT_CARRIER_ERROR, ex.getMessage());
        }
        return self.applyRegistration(shipmentId, reg);
    }

    @Transactional
    public ShipmentResult applyRegistration(String shipmentId, CarrierClient.Registration reg) {
        Shipment shipment = required(shipmentId);
        if (shipment.getTrackingCode() != null) {
            return mapper.toResult(shipment);
        }
        shipment.registerWithCarrier(reg.trackingCode(), reg.carrierOrderId(), reg.expectedDate());
        Shipment saved = shipmentRepository.save(shipment);
        eventPublisher.publish(shipment.pullEvents());
        return mapper.toResult(saved);
    }

    public ShipmentResult fetchLabel(FetchLabelCommand command) {
        Shipment shipment = self.loadShipment(command.shipmentId());
        ensureViewable(shipment, command.ownerId());
        if (shipment.getTrackingCode() == null) {
            throw new ShippingException(ShippingErrorCode.SHIPMENT_INVALID_STATE,
                    "Cannot fetch label before carrier registration");
        }
        String labelUrl = carrierClient.fetchLabel(shipment.getTrackingCode());
        return self.applyLabel(shipment.getShipmentId(), labelUrl);
    }

    @Transactional
    public ShipmentResult applyLabel(String shipmentId, String labelUrl) {
        Shipment shipment = required(shipmentId);
        shipment.fetchLabel(labelUrl);
        Shipment saved = shipmentRepository.save(shipment);
        eventPublisher.publish(shipment.pullEvents());
        return mapper.toResult(saved);
    }

    public ShipmentResult cancel(CancelShipmentCommand command) {
        Shipment shipment = self.loadShipment(command.shipmentId());
        String merchantId = merchantQueryPort.findMerchantIdByOwnerId(command.ownerId())
                .orElseThrow(() -> new ShippingException(ShippingErrorCode.SHIPMENT_FORBIDDEN));
        shipment.ensureOwnedByMerchant(merchantId);
        if (shipment.getTrackingCode() != null) {
            try {
                carrierClient.cancel(shipment.getTrackingCode(), command.reason());
            } catch (RuntimeException ex) {
                log.warn("Carrier cancel failed for {}: {}", shipment.getTrackingCode(), ex.getMessage());
            }
        }
        return self.applyCancel(command.shipmentId(), command.reason());
    }

    @Transactional
    public ShipmentResult applyCancel(String shipmentId, String reason) {
        Shipment shipment = required(shipmentId);
        shipment.cancel(reason);
        Shipment saved = shipmentRepository.save(shipment);
        eventPublisher.publish(shipment.pullEvents());
        return mapper.toResult(saved);
    }

    @Transactional
    public ShipmentResult resolveIssue(ResolveIssueCommand command) {
        Shipment shipment = required(command.shipmentId());
        shipment.resolveIssue(command.issueType(), command.resolution());
        Shipment saved = shipmentRepository.save(shipment);
        eventPublisher.publish(shipment.pullEvents());
        return mapper.toResult(saved);
    }

    @Transactional
    public ShipmentResult applyCarrierWebhook(CarrierWebhookCommand webhook) {
        Shipment shipment = shipmentRepository.findByTrackingCode(webhook.trackingCode())
                .orElseThrow(() -> new ShippingException(ShippingErrorCode.SHIPMENT_NOT_FOUND));
        switch (webhook.type()) {
            case "PICKED_UP" -> shipment.markPickedUp(webhook.warehouseId());
            case "IN_TRANSIT" -> shipment.updateInTransitStatus(webhook.currentLocation(), webhook.statusDesc());
            case "OUT_FOR_DELIVERY" -> shipment.markOutForDelivery(webhook.shipperName(), webhook.shipperPhone());
            case "DELIVERED" -> shipment.markDelivered(webhook.signatureUrl());
            case "DELIVERY_FAILED" -> shipment.recordDeliveryFailure(webhook.reason());
            case "RETURNED" -> shipment.markReturned(webhook.reason());
            case "RETRY" -> shipment.retryDelivery();
            default -> throw new ShippingException(ShippingErrorCode.INVALID_ARGUMENT,
                    "Unknown webhook type: " + webhook.type());
        }
        Shipment saved = shipmentRepository.save(shipment);
        eventPublisher.publish(shipment.pullEvents());
        publishIntegration(saved, webhook);
        return mapper.toResult(saved);
    }

    private void publishIntegration(Shipment saved, CarrierWebhookCommand webhook) {
        switch (webhook.type()) {
            case "PICKED_UP" -> integrationEventPublisher.publishDispatched(
                    saved.getShipmentId(), saved.getOrderId(), saved.getTrackingCode());
            case "DELIVERED" -> integrationEventPublisher.publishDelivered(
                    saved.getShipmentId(), saved.getOrderId(), webhook.signatureUrl(), saved.getDeliveredAt());
            case "DELIVERY_FAILED" -> integrationEventPublisher.publishDeliveryFailed(
                    saved.getShipmentId(), saved.getOrderId(), webhook.reason(), saved.getAttemptCount());
            default -> {
                /* in-transit / out-for-delivery / returned do not surface to other contexts */
            }
        }
    }

    @Transactional(readOnly = true)
    public ShippingQuoteResult quote(QuoteShippingCommand command) {
        String currency = command.currency() == null ? "VND" : command.currency();
        var rate = rateRepository.findByZoneCode(command.address().provinceCode());
        if (rate.isPresent()) {
            return new ShippingQuoteResult(rate.get().getBaseFee(), rate.get().getCurrency(),
                    rate.get().getZoneCode(), "configured-rate", rate.get().getCondition(), null, null);
        }
        CarrierClient.Quote q = carrierClient.quote(command.address(), command.dimensions(), currency);
        return new ShippingQuoteResult(q.fee(), q.currency(), q.zoneCode(), "carrier", q.detail(),
                q.expectedDeliveryDate(), q.orderDate());
    }

    @Transactional(readOnly = true)
    public ShipmentResult get(String shipmentId, String requesterUserId) {
        Shipment shipment = required(shipmentId);
        ensureViewable(shipment, requesterUserId);
        return mapper.toResult(shipment);
    }

    @Transactional(readOnly = true)
    public List<ShipmentResult> findByOrderId(String orderId, String requesterUserId) {
        String requesterMerchantId = requesterUserId == null ? null
                : merchantQueryPort.findMerchantIdByOwnerId(requesterUserId).orElse(null);
        return shipmentRepository.findByOrderId(orderId).stream()
                .filter(s -> {
                    try {
                        s.ensureViewableBy(requesterUserId, requesterMerchantId);
                        return true;
                    } catch (ShippingException ex) {
                        return false;
                    }
                })
                .map(mapper::toResult)
                .toList();
    }

    @Transactional(readOnly = true)
    public Shipment loadShipment(String shipmentId) {
        return required(shipmentId);
    }

    private void ensureViewable(Shipment shipment, String requesterUserId) {
        String merchantId = requesterUserId == null ? null
                : merchantQueryPort.findMerchantIdByOwnerId(requesterUserId).orElse(null);
        shipment.ensureViewableBy(requesterUserId, merchantId);
    }

    private Shipment required(String shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShippingException(ShippingErrorCode.SHIPMENT_NOT_FOUND));
    }
}
