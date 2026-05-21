package com.aionn.shipping.domain.model;

import com.aionn.sharedkernel.domain.Guard;
import com.aionn.sharedkernel.domain.model.AggregateRoot;
import com.aionn.shipping.domain.event.ShipmentEvents;
import com.aionn.shipping.domain.exception.ShippingErrorCode;
import com.aionn.shipping.domain.exception.ShippingException;
import com.aionn.shipping.domain.valueobject.ShipmentAddress;
import com.aionn.shipping.domain.valueobject.ShipmentDimensions;
import com.aionn.shipping.domain.valueobject.ShipmentStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
public class Shipment extends AggregateRoot {

    private final String shipmentId;
    private final String orderId;
    private final ShipmentAddress address;
    private final ShipmentDimensions dimensions;
    private BigDecimal codAmount;
    private BigDecimal shippingFee;
    private String currency;
    private String trackingCode;
    private String carrierOrderId;
    private String labelUrl;
    private String currentLocation;
    private String shipperName;
    private String shipperPhone;
    private String signatureUrl;
    private int attemptCount;
    private String lastFailureReason;
    private String issueType;
    private String issueResolution;
    private Instant expectedDeliveryDate;
    private ShipmentStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant pickedAt;
    private Instant deliveredAt;
    private Instant cancelledAt;
    private Instant returnedAt;

    public Shipment(
            String shipmentId,
            String orderId,
            ShipmentAddress address,
            ShipmentDimensions dimensions,
            BigDecimal codAmount,
            BigDecimal shippingFee,
            String currency,
            String trackingCode,
            String carrierOrderId,
            String labelUrl,
            String currentLocation,
            String shipperName,
            String shipperPhone,
            String signatureUrl,
            int attemptCount,
            String lastFailureReason,
            String issueType,
            String issueResolution,
            Instant expectedDeliveryDate,
            ShipmentStatus status,
            Instant createdAt,
            Instant updatedAt,
            Instant pickedAt,
            Instant deliveredAt,
            Instant cancelledAt,
            Instant returnedAt) {
        this.shipmentId = shipmentId;
        this.orderId = orderId;
        this.address = address;
        this.dimensions = dimensions;
        this.codAmount = codAmount;
        this.shippingFee = shippingFee;
        this.currency = currency;
        this.trackingCode = trackingCode;
        this.carrierOrderId = carrierOrderId;
        this.labelUrl = labelUrl;
        this.currentLocation = currentLocation;
        this.shipperName = shipperName;
        this.shipperPhone = shipperPhone;
        this.signatureUrl = signatureUrl;
        this.attemptCount = attemptCount;
        this.lastFailureReason = lastFailureReason;
        this.issueType = issueType;
        this.issueResolution = issueResolution;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.pickedAt = pickedAt;
        this.deliveredAt = deliveredAt;
        this.cancelledAt = cancelledAt;
        this.returnedAt = returnedAt;
    }

    public static Shipment request(
            String shipmentId,
            String orderId,
            ShipmentAddress address,
            ShipmentDimensions dimensions,
            BigDecimal codAmount,
            BigDecimal shippingFee,
            String currency) {
        Instant now = Instant.now();
        Shipment s = new Shipment(shipmentId, orderId, address, dimensions, codAmount, shippingFee, currency,
                null, null, null, null, null, null, null, 0, null, null, null, null,
                ShipmentStatus.REQUESTED, now, now, null, null, null, null);
        s.record(new ShipmentEvents.ShipmentRequested(shipmentId, orderId,
                dimensions.weightGram(), dimensionsString(dimensions),
                address.districtId(), address.wardCode(), now));
        return s;
    }

    public void registerWithCarrier(String trackingCode, String carrierOrderId, Instant expectedDate) {
        ensureTransition(ShipmentStatus.REGISTERED);
        this.trackingCode = trackingCode;
        this.carrierOrderId = carrierOrderId;
        this.expectedDeliveryDate = expectedDate;
        this.status = ShipmentStatus.REGISTERED;
        touch();
        record(new ShipmentEvents.ShipmentRegistered(shipmentId, trackingCode, carrierOrderId, expectedDate,
                updatedAt));
    }

    public void fetchLabel(String labelUrl) {
        Guard.require(status == ShipmentStatus.REGISTERED || status == ShipmentStatus.PICKED_UP,
                () -> new ShippingException(ShippingErrorCode.SHIPMENT_INVALID_STATE,
                        "Label only available after carrier registration"));
        this.labelUrl = labelUrl;
        touch();
        record(new ShipmentEvents.ShippingLabelFetched(shipmentId, trackingCode, labelUrl, updatedAt));
    }

    public void markPickedUp(String warehouseId) {
        ensureTransition(ShipmentStatus.PICKED_UP);
        Instant now = Instant.now();
        this.status = ShipmentStatus.PICKED_UP;
        this.pickedAt = now;
        this.updatedAt = now;
        record(new ShipmentEvents.ShipmentPickedUp(shipmentId, warehouseId, pickedAt, now));
    }

    public void updateInTransitStatus(String currentLocation, String statusDesc) {
        Guard.require(status == ShipmentStatus.PICKED_UP || status == ShipmentStatus.IN_TRANSIT,
                () -> new ShippingException(ShippingErrorCode.SHIPMENT_INVALID_STATE,
                        "Status updates only valid while in transit"));
        this.currentLocation = currentLocation;
        if (status == ShipmentStatus.PICKED_UP) {
            this.status = ShipmentStatus.IN_TRANSIT;
        }
        touch();
        record(new ShipmentEvents.ShipmentStatusUpdated(shipmentId, status.name(), currentLocation, statusDesc,
                updatedAt));
    }

    public void markOutForDelivery(String shipperName, String shipperPhone) {
        ensureTransition(ShipmentStatus.OUT_FOR_DELIVERY);
        this.shipperName = shipperName;
        this.shipperPhone = shipperPhone;
        this.status = ShipmentStatus.OUT_FOR_DELIVERY;
        touch();
        record(new ShipmentEvents.ShipmentOutForDelivery(shipmentId, shipperName, shipperPhone, updatedAt));
    }

    public void markDelivered(String signatureUrl) {
        ensureTransition(ShipmentStatus.DELIVERED);
        Instant now = Instant.now();
        this.signatureUrl = signatureUrl;
        this.status = ShipmentStatus.DELIVERED;
        this.deliveredAt = now;
        this.updatedAt = now;
        record(new ShipmentEvents.ShipmentDelivered(shipmentId, orderId, signatureUrl, deliveredAt, now));
    }

    public void recordDeliveryFailure(String reason) {
        ensureTransition(ShipmentStatus.DELIVERY_FAILED);
        this.attemptCount++;
        this.lastFailureReason = reason;
        this.status = ShipmentStatus.DELIVERY_FAILED;
        touch();
        record(new ShipmentEvents.ShipmentDeliveryFailed(shipmentId, reason, attemptCount, updatedAt));
    }

    public void retryDelivery() {
        Guard.require(status == ShipmentStatus.DELIVERY_FAILED,
                () -> new ShippingException(ShippingErrorCode.SHIPMENT_INVALID_STATE,
                        "Only failed deliveries can be retried"));
        this.status = ShipmentStatus.OUT_FOR_DELIVERY;
        touch();
        record(new ShipmentEvents.ShipmentStatusUpdated(shipmentId, status.name(), currentLocation,
                "retry attempt " + attemptCount, updatedAt));
    }

    public void markReturned(String returnReason) {
        ensureTransition(ShipmentStatus.RETURNED);
        Instant now = Instant.now();
        this.status = ShipmentStatus.RETURNED;
        this.returnedAt = now;
        this.updatedAt = now;
        record(new ShipmentEvents.ShipmentReturned(shipmentId, returnReason, returnedAt, now));
    }

    public void cancel(String reason) {
        Guard.require(!status.isPickedUp(),
                () -> new ShippingException(ShippingErrorCode.SHIPMENT_ALREADY_PICKED_UP));
        ensureTransition(ShipmentStatus.CANCELLED);
        Instant now = Instant.now();
        this.status = ShipmentStatus.CANCELLED;
        this.cancelledAt = now;
        this.updatedAt = now;
        record(new ShipmentEvents.ShipmentCancelled(shipmentId, reason, cancelledAt, now));
    }

    public void resolveIssue(String issueType, String resolution) {
        this.issueType = issueType;
        this.issueResolution = resolution;
        Instant now = Instant.now();
        this.updatedAt = now;
        record(new ShipmentEvents.ShippingIssueResolved(shipmentId, issueType, resolution, now, now));
    }

    private static String dimensionsString(ShipmentDimensions d) {
        return d.lengthCm() + "x" + d.widthCm() + "x" + d.heightCm() + " cm, " + d.weightGram() + "g";
    }

    private void ensureTransition(ShipmentStatus next) {
        Guard.require(status.canTransitionTo(next),
                () -> new ShippingException(ShippingErrorCode.SHIPMENT_INVALID_STATE,
                        "Cannot transition shipment from " + status + " to " + next));
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @Override
    protected String aggregateId() {
        return shipmentId;
    }
}
