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

    private static final java.math.BigDecimal DEFAULT_COMMISSION_RATE =
            new java.math.BigDecimal("0.0500");

    private final String merchantId;
    private final String ownerId;
    private String name;
    private String logoUrl;
    private String description;
    private String provinceCode;
    private String provinceName;
    private MerchantStatus status;
    private java.math.BigDecimal commissionRate;
    private String stripeAccountId;
    private boolean stripeChargesEnabled;
    private boolean stripePayoutsEnabled;
    private final Instant createdAt;
    private Instant updatedAt;

    public Merchant(
            String merchantId,
            String ownerId,
            String name,
            String logoUrl,
            String description,
            String provinceCode,
            String provinceName,
            MerchantStatus status,
            java.math.BigDecimal commissionRate,
            String stripeAccountId,
            boolean stripeChargesEnabled,
            boolean stripePayoutsEnabled,
            Instant createdAt,
            Instant updatedAt) {
        this.merchantId = merchantId;
        this.ownerId = ownerId;
        this.name = name;
        this.logoUrl = logoUrl;
        this.description = description;
        this.provinceCode = provinceCode;
        this.provinceName = provinceName;
        this.status = status;
        this.commissionRate = commissionRate != null ? commissionRate : DEFAULT_COMMISSION_RATE;
        this.stripeAccountId = stripeAccountId;
        this.stripeChargesEnabled = stripeChargesEnabled;
        this.stripePayoutsEnabled = stripePayoutsEnabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Merchant register(String merchantId, String ownerId, String name) {
        Instant now = Instant.now();
        Merchant merchant = new Merchant(merchantId, ownerId, name, null, null, null, null,
                MerchantStatus.PENDING, DEFAULT_COMMISSION_RATE, null, false, false, now, now);
        merchant.record(new MerchantEvents.MerchantRegistered(
                merchantId, ownerId, name, MerchantStatus.PENDING.name(), now));
        return merchant;
    }

    public void linkStripeAccount(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
        this.updatedAt = Instant.now();
    }

    public void updateStripeCapabilities(boolean chargesEnabled, boolean payoutsEnabled) {
        this.stripeChargesEnabled = chargesEnabled;
        this.stripePayoutsEnabled = payoutsEnabled;
        this.updatedAt = Instant.now();
    }

    public void updateCommissionRate(java.math.BigDecimal newRate) {
        Guard.require(newRate != null && newRate.signum() >= 0
                && newRate.compareTo(java.math.BigDecimal.ONE) <= 0,
                () -> new CatalogException(CatalogErrorCode.INVALID_ARGUMENT,
                        "commission rate must be between 0 and 1"));
        this.commissionRate = newRate;
        this.updatedAt = Instant.now();
    }

    /**
     * Update display profile + storefront province. {@code provinceCode} is
     * authoritative; {@code provinceName} is a denormalized snapshot supplied
     * by the application service after validating the code against identity.
     * Pass {@code null} for both to clear, or for both to leave unchanged from
     * the caller's perspective — the service decides whether the update should
     * include a province change.
     */
    public void updateProfile(String name, String logoUrl, String description,
                              String provinceCode, String provinceName) {
        Guard.require(name != null && !name.isBlank(),
                () -> new CatalogException(CatalogErrorCode.INVALID_ARGUMENT, "name must not be blank"));
        this.name = name.trim();
        this.logoUrl = logoUrl;
        this.description = description;
        boolean provinceChanged = !java.util.Objects.equals(this.provinceCode, provinceCode);
        this.provinceCode = provinceCode;
        this.provinceName = provinceName;
        this.updatedAt = Instant.now();
        if (this.status == MerchantStatus.PENDING) {
            this.status = MerchantStatus.ACTIVE;
        }
        record(new MerchantEvents.MerchantProfileUpdated(merchantId, this.name, logoUrl, description,
                provinceCode, provinceName, provinceChanged, updatedAt));
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
