package com.aionn.catalog.domain.valueobject;

/**
 * Lifecycle for a merchant storefront.
 *
 * <ul>
 * <li>{@code PENDING}: created from KYC upgrade, waiting for first profile
 * fill.</li>
 * <li>{@code ACTIVE}: storefront is published, products are searchable.</li>
 * <li>{@code SUSPENDED}: temporarily hidden by CS Admin; products are also
 * hidden.</li>
 * <li>{@code CLOSED}: permanent closure; cannot transition out.</li>
 * </ul>
 */
public enum MerchantStatus {
    PENDING,
    ACTIVE,
    SUSPENDED,
    CLOSED;

    public boolean canTransitionTo(MerchantStatus next) {
        return switch (this) {
            case PENDING -> next == ACTIVE || next == CLOSED;
            case ACTIVE -> next == SUSPENDED || next == CLOSED;
            case SUSPENDED -> next == ACTIVE || next == CLOSED;
            case CLOSED -> false;
        };
    }
}

