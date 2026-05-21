package com.aionn.promotion.domain.model;

import com.aionn.sharedkernel.domain.Guard;
import com.aionn.sharedkernel.domain.model.AggregateRoot;
import com.aionn.promotion.domain.event.PromotionEvents;
import com.aionn.promotion.domain.exception.PromotionErrorCode;
import com.aionn.promotion.domain.exception.PromotionException;
import com.aionn.sharedkernel.domain.vo.Money;
import lombok.Getter;

import java.time.Instant;

@Getter
public class Voucher extends AggregateRoot {

    private final String voucherCode;
    private final String campaignId;
    private final Money discountAmount;
    private final int usageLimit;
    private int usedCount;
    private int reservedCount;
    private final Instant validFrom;
    private final Instant validUntil;
    private final Instant createdAt;
    private Instant updatedAt;

    public Voucher(String voucherCode, String campaignId, Money discountAmount,
            int usageLimit, int usedCount, int reservedCount,
            Instant validFrom, Instant validUntil, Instant createdAt, Instant updatedAt) {
        this.voucherCode = voucherCode;
        this.campaignId = campaignId;
        this.discountAmount = discountAmount;
        this.usageLimit = usageLimit;
        this.usedCount = usedCount;
        this.reservedCount = reservedCount;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Voucher issue(String voucherCode, String campaignId, Money discountAmount,
            int usageLimit, Instant validFrom, Instant validUntil) {
        Guard.require(usageLimit > 0,
                () -> new PromotionException(PromotionErrorCode.INVALID_ARGUMENT, "usageLimit must be > 0"));
        Guard.require(validUntil == null || validFrom == null || validFrom.isBefore(validUntil),
                () -> new PromotionException(PromotionErrorCode.INVALID_ARGUMENT,
                        "validFrom must be before validUntil"));
        Instant now = Instant.now();
        Voucher v = new Voucher(voucherCode, campaignId, discountAmount, usageLimit, 0, 0,
                validFrom, validUntil, now, now);
        v.record(new PromotionEvents.VoucherIssued(voucherCode, campaignId,
                discountAmount.amount(), discountAmount.currency(),
                usageLimit, validUntil, now));
        return v;
    }

    public boolean isUsable(Instant now) {
        if (validUntil != null && now.isAfter(validUntil))
            return false;
        if (validFrom != null && now.isBefore(validFrom))
            return false;
        return reservedCount + usedCount < usageLimit;
    }

    public int remainingUses() {
        return usageLimit - reservedCount - usedCount;
    }

    public void reserveSlot() {
        Instant now = Instant.now();
        Guard.require(validUntil == null || !now.isAfter(validUntil),
                () -> new PromotionException(PromotionErrorCode.VOUCHER_EXPIRED));
        Guard.require(remainingUses() > 0,
                () -> new PromotionException(PromotionErrorCode.VOUCHER_NO_USAGE_LEFT));
        this.reservedCount++;
        this.updatedAt = now;
    }

    public void commitSlot() {
        Guard.require(reservedCount > 0,
                () -> new PromotionException(PromotionErrorCode.USER_VOUCHER_INVALID_STATE,
                        "No reserved slot to commit"));
        this.reservedCount--;
        this.usedCount++;
        this.updatedAt = Instant.now();
    }

    public void releaseSlot() {
        if (reservedCount <= 0)
            return;
        this.reservedCount--;
        this.updatedAt = Instant.now();
    }

    @Override
    protected String aggregateId() {
        return voucherCode;
    }
}
