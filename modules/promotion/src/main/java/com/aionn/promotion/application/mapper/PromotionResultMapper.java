package com.aionn.promotion.application.mapper;

import com.aionn.promotion.application.dto.campaign.result.CampaignResult;
import com.aionn.promotion.application.dto.voucher.result.UserVoucherResult;
import com.aionn.promotion.application.dto.voucher.result.VoucherResult;
import com.aionn.promotion.domain.model.PromotionCampaign;
import com.aionn.promotion.domain.model.UserVoucher;
import com.aionn.promotion.domain.model.Voucher;
import org.springframework.stereotype.Component;

@Component
public class PromotionResultMapper {

    public CampaignResult toResult(PromotionCampaign c) {
        return new CampaignResult(
                c.getCampaignId(),
                c.getName(),
                c.getType().name(),
                c.getBudget().amount(),
                c.getBudgetRemaining().amount(),
                c.getBudget().currency(),
                c.getStartDate(),
                c.getEndDate(),
                c.getCreatedBy(),
                c.getStatus().name(),
                c.getCondition().minOrderValue(),
                c.getCondition().applicableCategoryIds(),
                c.getCondition().maxClaimsPerUser(),
                c.getCondition().maxUsesPerVoucher(),
                c.getCreatedAt(),
                c.getUpdatedAt());
    }

    public VoucherResult toResult(Voucher v) {
        return new VoucherResult(
                v.getVoucherCode(),
                v.getCampaignId(),
                v.getDiscountAmount().amount(),
                v.getDiscountAmount().currency(),
                v.getUsageLimit(),
                v.getUsedCount(),
                v.getReservedCount(),
                v.getValidFrom(),
                v.getValidUntil(),
                v.getCreatedAt(),
                v.getUpdatedAt());
    }

    public UserVoucherResult toResult(UserVoucher u) {
        return new UserVoucherResult(
                u.getUserVoucherId(),
                u.getVoucherCode(),
                u.getUserId(),
                u.getStatus().name(),
                u.getReservedOrderId(),
                u.getAppliedAmount() == null ? null : u.getAppliedAmount().amount(),
                u.getAppliedAmount() == null ? null : u.getAppliedAmount().currency(),
                u.getClaimedAt(),
                u.getReservedAt(),
                u.getReservedExpiresAt(),
                u.getAppliedAt(),
                u.getReleasedAt(),
                u.getUpdatedAt());
    }
}

