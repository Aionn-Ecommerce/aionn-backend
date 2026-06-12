package com.aionn.promotion.domain.model;

import com.aionn.sharedkernel.domain.Guard;
import com.aionn.sharedkernel.domain.model.AggregateRoot;
import com.aionn.promotion.domain.event.PromotionEvents;
import com.aionn.promotion.domain.exception.PromotionErrorCode;
import com.aionn.promotion.domain.exception.PromotionException;
import com.aionn.promotion.domain.valueobject.CampaignStatus;
import com.aionn.promotion.domain.valueobject.CampaignType;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.promotion.domain.valueobject.PromotionCondition;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
public class PromotionCampaign extends AggregateRoot {

    private final String campaignId;
    private final String name;
    private final CampaignType type;
    private final Money budget;
    private Money budgetRemaining;
    private final Instant startDate;
    private final Instant endDate;
    private final String createdBy;
    private CampaignStatus status;
    private PromotionCondition condition;
    private final Instant createdAt;
    private Instant updatedAt;

    public PromotionCampaign(
            String campaignId,
            String name,
            CampaignType type,
            Money budget,
            Money budgetRemaining,
            Instant startDate,
            Instant endDate,
            String createdBy,
            CampaignStatus status,
            PromotionCondition condition,
            Instant createdAt,
            Instant updatedAt) {
        this.campaignId = campaignId;
        this.name = name;
        this.type = type;
        this.budget = budget;
        this.budgetRemaining = budgetRemaining == null ? budget : budgetRemaining;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
        this.status = status;
        this.condition = condition == null ? PromotionCondition.empty() : condition;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PromotionCampaign create(String campaignId, String name, CampaignType type,
            Money budget, Instant startDate, Instant endDate, String createdBy) {
        Guard.require(startDate != null && endDate != null && startDate.isBefore(endDate),
                () -> new PromotionException(PromotionErrorCode.INVALID_ARGUMENT,
                        "startDate must be before endDate"));
        Instant now = Instant.now();
        Guard.require(!startDate.isBefore(now.minusSeconds(60)),
                () -> new PromotionException(PromotionErrorCode.INVALID_ARGUMENT,
                        "startDate must not be in the past"));
        // New campaigns are SCHEDULED; the periodic worker (or an explicit
        // activate call) flips them to RUNNING when startDate is reached.
        PromotionCampaign c = new PromotionCampaign(campaignId, name, type, budget, budget,
                startDate, endDate, createdBy, CampaignStatus.SCHEDULED, PromotionCondition.empty(), now, now);
        c.record(new PromotionEvents.PromotionCampaignCreated(campaignId, name,
                budget.amount(), budget.currency(), startDate, endDate, createdBy, now));
        return c;
    }

    public void activate() {
        Guard.require(status == CampaignStatus.SCHEDULED || status == CampaignStatus.DRAFT,
                () -> new PromotionException(PromotionErrorCode.CAMPAIGN_INVALID_STATE,
                        "Cannot activate from " + status));
        if (!status.canTransitionTo(CampaignStatus.RUNNING)) {
            // DRAFT cannot transition directly; promote it via SCHEDULED first.
            this.status = CampaignStatus.SCHEDULED;
        }
        this.status = CampaignStatus.RUNNING;
        Instant now = Instant.now();
        this.updatedAt = now;
        record(new PromotionEvents.PromotionCampaignActivated(campaignId, now));
    }

    public void schedule() {
        Guard.require(status.canTransitionTo(CampaignStatus.SCHEDULED),
                () -> new PromotionException(PromotionErrorCode.CAMPAIGN_INVALID_STATE,
                        "Cannot schedule from " + status));
        this.status = CampaignStatus.SCHEDULED;
        Instant now = Instant.now();
        this.updatedAt = now;
    }

    public void end() {
        Guard.require(status.canTransitionTo(CampaignStatus.ENDED),
                () -> new PromotionException(PromotionErrorCode.CAMPAIGN_INVALID_STATE,
                        "Cannot end from " + status));
        this.status = CampaignStatus.ENDED;
        Instant now = Instant.now();
        this.updatedAt = now;
        record(new PromotionEvents.PromotionCampaignEnded(campaignId, now, now));
    }

    public void cancel(String reason) {
        Guard.require(status.canTransitionTo(CampaignStatus.CANCELLED),
                () -> new PromotionException(PromotionErrorCode.CAMPAIGN_INVALID_STATE,
                        "Cannot cancel from " + status));
        this.status = CampaignStatus.CANCELLED;
        Instant now = Instant.now();
        this.updatedAt = now;
        record(new PromotionEvents.PromotionCampaignCancelled(campaignId, reason, now, now));
    }

    public void configureCondition(PromotionCondition condition) {
        Guard.notNull(condition, "condition");
        this.condition = condition;
        Instant now = Instant.now();
        this.updatedAt = now;
        record(new PromotionEvents.PromotionConditionConfigured(campaignId,
                condition.minOrderValue(), condition.applicableCategoryIds(),
                condition.maxClaimsPerUser(), condition.maxUsesPerVoucher(), now, now));
    }

    public void ensureRunning() {
        Instant now = Instant.now();
        if (status == CampaignStatus.SCHEDULED && !now.isBefore(startDate)) {
            this.status = CampaignStatus.RUNNING;
            record(new PromotionEvents.PromotionCampaignActivated(campaignId, now));
        }
        if (status == CampaignStatus.RUNNING && now.isAfter(endDate)) {
            end();
        }
        Guard.require(status == CampaignStatus.RUNNING,
                () -> new PromotionException(PromotionErrorCode.CAMPAIGN_NOT_RUNNING,
                        "Campaign is " + status));
    }

    public void consumeBudget(Money amount) {
        Guard.require(budgetRemaining.isGreaterOrEqual(amount),
                () -> new PromotionException(PromotionErrorCode.CAMPAIGN_OUT_OF_BUDGET));
        this.budgetRemaining = budgetRemaining.subtract(amount);
        this.updatedAt = Instant.now();
    }

    public void releaseBudget(Money amount) {
        BigDecimal newRemaining = budgetRemaining.amount().add(amount.amount());
        if (newRemaining.compareTo(budget.amount()) > 0) {
            newRemaining = budget.amount();
        }
        this.budgetRemaining = Money.of(newRemaining, budget.currency());
        this.updatedAt = Instant.now();
    }

    @Override
    protected String aggregateId() {
        return campaignId;
    }
}
