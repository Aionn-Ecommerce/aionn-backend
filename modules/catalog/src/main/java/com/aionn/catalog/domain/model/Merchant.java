package com.aionn.catalog.domain.model;

import com.aionn.sharedkernel.domain.Guard;
import com.aionn.sharedkernel.domain.model.AggregateRoot;
import com.aionn.catalog.domain.event.MerchantEvents;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.valueobject.MerchantStatus;
import lombok.Getter;

import java.time.Instant;

@Getter
public class Merchant extends AggregateRoot {

    private final String merchantId;
    private final String ownerId;
    private String name;
    private String logoUrl;
    private String description;
    private MerchantStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Merchant(
            String merchantId,
            String ownerId,
            String name,
            String logoUrl,
            String description,
            MerchantStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.merchantId = merchantId;
        this.ownerId = ownerId;
        this.name = name;
        this.logoUrl = logoUrl;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Merchant register(String merchantId, String ownerId, String name) {
        Instant now = Instant.now();
        Merchant merchant = new Merchant(merchantId, ownerId, name, null, null,
                MerchantStatus.PENDING, now, now);
        merchant.record(new MerchantEvents.MerchantRegistered(
                merchantId, ownerId, name, MerchantStatus.PENDING.name(), now));
        return merchant;
    }

    public void updateProfile(String name, String logoUrl, String description) {
        Guard.require(name != null && !name.isBlank(),
                () -> new CatalogException(CatalogErrorCode.INVALID_ARGUMENT, "name must not be blank"));
        this.name = name.trim();
        this.logoUrl = logoUrl;
        this.description = description;
        this.updatedAt = Instant.now();
        if (this.status == MerchantStatus.PENDING) {
            this.status = MerchantStatus.ACTIVE;
        }
        record(new MerchantEvents.MerchantProfileUpdated(merchantId, this.name, logoUrl, description, updatedAt));
    }

    public void suspend(String adminId, String reason) {
        ensureTransitionAllowed(MerchantStatus.SUSPENDED);
        this.status = MerchantStatus.SUSPENDED;
        this.updatedAt = Instant.now();
        record(new MerchantEvents.MerchantSuspended(merchantId, reason, adminId, updatedAt, updatedAt));
    }

    public void activate(String adminId, String reason) {
        ensureTransitionAllowed(MerchantStatus.ACTIVE);
        this.status = MerchantStatus.ACTIVE;
        this.updatedAt = Instant.now();
        record(new MerchantEvents.MerchantActivated(merchantId, adminId, reason, updatedAt, updatedAt));
    }

    public void close(String reason) {
        ensureTransitionAllowed(MerchantStatus.CLOSED);
        this.status = MerchantStatus.CLOSED;
        this.updatedAt = Instant.now();
        record(new MerchantEvents.MerchantClosed(merchantId, reason, updatedAt, updatedAt));
    }

    private void ensureTransitionAllowed(MerchantStatus next) {
        Guard.require(status.canTransitionTo(next),
                () -> new CatalogException(CatalogErrorCode.MERCHANT_INVALID_TRANSITION,
                        "Cannot transition merchant from " + status + " to " + next));
    }

    @Override
    protected String aggregateId() {
        return merchantId;
    }
}
