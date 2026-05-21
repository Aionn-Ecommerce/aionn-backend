package com.aionn.promotion.domain.model;

import com.aionn.sharedkernel.domain.Guard;
import com.aionn.sharedkernel.domain.model.AggregateRoot;
import com.aionn.promotion.domain.event.PromotionEvents;
import com.aionn.promotion.domain.exception.PromotionErrorCode;
import com.aionn.promotion.domain.exception.PromotionException;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.promotion.domain.valueobject.UserVoucherStatus;
import lombok.Getter;

import java.time.Instant;

@Getter
public class UserVoucher extends AggregateRoot {

    private final String userVoucherId;
    private final String voucherCode;
    private final String userId;
    private UserVoucherStatus status;
    private String reservedOrderId;
    private Money appliedAmount;
    private final Instant claimedAt;
    private Instant reservedAt;
    private Instant reservedExpiresAt;
    private Instant appliedAt;
    private Instant releasedAt;
    private Instant updatedAt;

    public UserVoucher(String userVoucherId, String voucherCode, String userId,
            UserVoucherStatus status, String reservedOrderId, Money appliedAmount,
            Instant claimedAt, Instant reservedAt, Instant reservedExpiresAt,
            Instant appliedAt, Instant releasedAt, Instant updatedAt) {
        this.userVoucherId = userVoucherId;
        this.voucherCode = voucherCode;
        this.userId = userId;
        this.status = status;
        this.reservedOrderId = reservedOrderId;
        this.appliedAmount = appliedAmount;
        this.claimedAt = claimedAt;
        this.reservedAt = reservedAt;
        this.reservedExpiresAt = reservedExpiresAt;
        this.appliedAt = appliedAt;
        this.releasedAt = releasedAt;
        this.updatedAt = updatedAt;
    }

    public static UserVoucher claim(String userVoucherId, String voucherCode, String userId) {
        Instant now = Instant.now();
        UserVoucher uv = new UserVoucher(userVoucherId, voucherCode, userId,
                UserVoucherStatus.CLAIMED, null, null, now, null, null, null, null, now);
        uv.record(new PromotionEvents.VoucherClaimed(voucherCode, userId, now, now));
        return uv;
    }

    public void reserve(String orderId, Instant expiresAt) {
        ensureTransition(UserVoucherStatus.RESERVED);
        Guard.require(status != UserVoucherStatus.RESERVED || orderId.equals(reservedOrderId),
                () -> new PromotionException(PromotionErrorCode.USER_VOUCHER_RESERVED_BY_OTHER));
        this.status = UserVoucherStatus.RESERVED;
        this.reservedOrderId = orderId;
        this.reservedAt = Instant.now();
        this.reservedExpiresAt = expiresAt;
        this.updatedAt = reservedAt;
        record(new PromotionEvents.VoucherReserved(voucherCode, userId, orderId,
                reservedAt, expiresAt, reservedAt));
    }

    public void apply(Money amount) {
        Guard.require(status == UserVoucherStatus.RESERVED,
                () -> new PromotionException(PromotionErrorCode.USER_VOUCHER_INVALID_STATE,
                        "Voucher must be RESERVED to apply"));
        this.status = UserVoucherStatus.APPLIED;
        this.appliedAmount = amount;
        Instant now = Instant.now();
        this.appliedAt = now;
        this.updatedAt = now;
        record(new PromotionEvents.VoucherApplied(voucherCode, userId, reservedOrderId,
                amount.amount(), amount.currency(), appliedAt, appliedAt));
    }

    public void release(String reason) {
        Guard.require(status == UserVoucherStatus.RESERVED,
                () -> new PromotionException(PromotionErrorCode.USER_VOUCHER_INVALID_STATE,
                        "Only RESERVED vouchers can be released"));
        String orderId = reservedOrderId;
        this.status = UserVoucherStatus.RELEASED;
        Instant now = Instant.now();
        this.releasedAt = now;
        this.updatedAt = now;
        this.reservedOrderId = null;
        this.reservedAt = null;
        this.reservedExpiresAt = null;
        record(new PromotionEvents.VoucherReleased(voucherCode, userId, orderId, reason, now, now));
    }

    public void expire() {
        if (status == UserVoucherStatus.APPLIED)
            return;
        if (status == UserVoucherStatus.EXPIRED)
            return;
        this.status = UserVoucherStatus.EXPIRED;
        Instant now = Instant.now();
        this.updatedAt = now;
    }

    public boolean isReservationExpired(Instant now) {
        return status == UserVoucherStatus.RESERVED && reservedExpiresAt != null
                && now.isAfter(reservedExpiresAt);
    }

    private void ensureTransition(UserVoucherStatus next) {
        Guard.require(status.canTransitionTo(next),
                () -> new PromotionException(PromotionErrorCode.USER_VOUCHER_INVALID_STATE,
                        "Cannot transition user voucher from " + status + " to " + next));
    }

    @Override
    protected String aggregateId() {
        return userVoucherId;
    }
}
