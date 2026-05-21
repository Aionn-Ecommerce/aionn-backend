package com.aionn.shipping.application.service;

import com.aionn.shipping.application.dto.rate.result.ShippingQuoteResult;
import com.aionn.shipping.application.dto.shipment.command.ShipmentCommands;
import com.aionn.shipping.application.dto.shipment.result.ShipmentResult;
import com.aionn.shipping.application.mapper.ShippingResultMapper;
import com.aionn.shipping.application.port.out.CarrierClient;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.shipping.application.port.out.ShipmentRepository;
import com.aionn.shipping.application.port.out.ShippingRateRepository;
import com.aionn.shipping.domain.exception.ShippingErrorCode;
import com.aionn.shipping.domain.exception.ShippingException;
import com.aionn.shipping.domain.model.Shipment;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShippingRateRepository rateRepository;
    private final ShippingResultMapper mapper;
    private final EventPublisher eventPublisher;
    private final CarrierClient carrierClient;

    public ShipmentResult createShipment(ShipmentCommands.CreateShipment command) {
        Shipment shipment = Shipment.request(IdGenerator.ulid(), command.orderId(),
                command.address(), command.dimensions(), command.codAmount(),
                command.shippingFee(), command.currency());
        Shipment saved = shipmentRepository.save(shipment);
        eventPublisher.publish(shipment.pullEvents());
        return mapper.toResult(saved);
    }

    public ShipmentResult registerWithCarrier(String shipmentId) {
        Shipment shipment = required(shipmentId);
        try {
            CarrierClient.Registration reg = carrierClient.register(
                    shipment.getShipmentId(),
                    shipment.getOrderId(),
                    shipment.getAddress(),
                    shipment.getDimensions(),
                    shipment.getCodAmount(),
                    shipment.getShippingFee(),
                    shipment.getCurrency());
            shipment.registerWithCarrier(reg.trackingCode(), reg.carrierOrderId(), reg.expectedDate());
        } catch (ShippingException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ShippingException(ShippingErrorCode.SHIPMENT_CARRIER_ERROR, ex.getMessage());
        }
        Shipment saved = shipmentRepository.save(shipment);
        eventPublisher.publish(shipment.pullEvents());
        return mapper.toResult(saved);
    }

    public ShipmentResult fetchLabel(ShipmentCommands.FetchLabel command) {
        Shipment shipment = required(command.shipmentId());
        if (shipment.getTrackingCode() == null) {
            throw new ShippingException(ShippingErrorCode.SHIPMENT_INVALID_STATE,
                    "Cannot fetch label before carrier registration");
        }
        String labelUrl = carrierClient.fetchLabel(shipment.getTrackingCode());
        shipment.fetchLabel(labelUrl);
        Shipment saved = shipmentRepository.save(shipment);
        eventPublisher.publish(shipment.pullEvents());
        return mapper.toResult(saved);
    }

    public ShipmentResult cancel(ShipmentCommands.CancelShipment command) {
        Shipment shipment = required(command.shipmentId());
        if (shipment.getTrackingCode() != null) {
            try {
                carrierClient.cancel(shipment.getTrackingCode(), command.reason());
            } catch (RuntimeException ex) {
                log.warn("Carrier cancel failed for {}: {}", shipment.getTrackingCode(), ex.getMessage());
            }
        }
        shipment.cancel(command.reason());
        Shipment saved = shipmentRepository.save(shipment);
        eventPublisher.publish(shipment.pullEvents());
        return mapper.toResult(saved);
    }

    public ShipmentResult resolveIssue(ShipmentCommands.ResolveIssue command) {
        Shipment shipment = required(command.shipmentId());
        shipment.resolveIssue(command.issueType(), command.resolution());
        Shipment saved = shipmentRepository.save(shipment);
        eventPublisher.publish(shipment.pullEvents());
        return mapper.toResult(saved);
    }

    
    public ShipmentResult applyCarrierWebhook(ShipmentCommands.CarrierWebhook webhook) {
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
        return mapper.toResult(saved);
    }

    
    @Transactional(readOnly = true)
    public ShippingQuoteResult quote(ShipmentCommands.QuoteShipping command) {
        String currency = command.currency() == null ? "VND" : command.currency();
        // Try configured rate by zone code (province) first.
        var rate = rateRepository.findByZoneCode(command.address().provinceCode());
        if (rate.isPresent()) {
            return new ShippingQuoteResult(rate.get().getBaseFee(), rate.get().getCurrency(),
                    rate.get().getZoneCode(), "configured-rate", rate.get().getCondition());
        }
        CarrierClient.Quote q = carrierClient.quote(command.address(), command.dimensions(), currency);
        return new ShippingQuoteResult(q.fee(), q.currency(), q.zoneCode(), "carrier", q.detail());
    }

    @Transactional(readOnly = true)
    public ShipmentResult get(String shipmentId) {
        return mapper.toResult(required(shipmentId));
    }

    private Shipment required(String shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShippingException(ShippingErrorCode.SHIPMENT_NOT_FOUND));
    }
}

