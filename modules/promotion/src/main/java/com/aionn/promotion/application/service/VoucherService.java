package com.aionn.promotion.application.service;

import com.aionn.promotion.application.dto.voucher.command.VoucherCommands;
import com.aionn.promotion.application.dto.voucher.result.UserVoucherResult;
import com.aionn.promotion.application.mapper.PromotionResultMapper;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.promotion.application.port.out.PromotionCampaignPersistencePort;
import com.aionn.promotion.application.port.out.UserVoucherPersistencePort;
import com.aionn.promotion.application.port.out.VoucherPersistencePort;
import com.aionn.promotion.domain.exception.PromotionErrorCode;
import com.aionn.promotion.domain.exception.PromotionException;
import com.aionn.promotion.domain.model.PromotionCampaign;
import com.aionn.promotion.domain.model.UserVoucher;
import com.aionn.promotion.domain.model.Voucher;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VoucherService {

    private static final Duration DEFAULT_RESERVATION_TTL = Duration.ofMinutes(15);

    private final VoucherPersistencePort voucherRepository;
    private final UserVoucherPersistencePort userVoucherRepository;
    private final PromotionCampaignPersistencePort campaignRepository;
    private final PromotionResultMapper mapper;
    private final EventPublisher eventPublisher;

    public UserVoucherResult claim(VoucherCommands.ClaimVoucher command) {
        Voucher voucher = voucherRepository.lockByCode(command.voucherCode())
                .orElseThrow(() -> new PromotionException(PromotionErrorCode.VOUCHER_NOT_FOUND));
        PromotionCampaign campaign = campaignRepository.findById(voucher.getCampaignId())
                .orElseThrow(() -> new PromotionException(PromotionErrorCode.CAMPAIGN_NOT_FOUND));
        campaign.ensureRunning();

        userVoucherRepository.findByUserAndCode(command.userId(), command.voucherCode())
                .ifPresent(existing -> {
                    throw new PromotionException(PromotionErrorCode.USER_VOUCHER_ALREADY_CLAIMED);
                });

        Integer maxClaims = campaign.getCondition().maxClaimsPerUserOrNull();
        if (maxClaims != null) {
            long claimed = userVoucherRepository.countByUserAndCampaign(command.userId(), campaign.getCampaignId());
            if (claimed >= maxClaims) {
                throw new PromotionException(PromotionErrorCode.USER_VOUCHER_LIMIT_REACHED);
            }
        }
        // Claim does not consume an inventory slot â€” that's done at reserve.
        UserVoucher uv = UserVoucher.claim(IdGenerator.ulid(), command.voucherCode(), command.userId());
        UserVoucher saved = userVoucherRepository.save(uv);
        eventPublisher.publish(uv.pullEvents());
        return mapper.toResult(saved);
    }

    public UserVoucherResult reserve(VoucherCommands.ReserveVoucher command) {
        Voucher voucher = voucherRepository.lockByCode(command.voucherCode())
                .orElseThrow(() -> new PromotionException(PromotionErrorCode.VOUCHER_NOT_FOUND));
        PromotionCampaign campaign = campaignRepository.findById(voucher.getCampaignId())
                .orElseThrow(() -> new PromotionException(PromotionErrorCode.CAMPAIGN_NOT_FOUND));
        campaign.ensureRunning();

        if (!campaign.getCondition().matches(command.orderValue(), command.orderCategoryIds())) {
            throw new PromotionException(PromotionErrorCode.CONDITION_NOT_MET);
        }

        UserVoucher uv = userVoucherRepository.findByUserAndCode(command.userId(), command.voucherCode())
                .orElseThrow(() -> new PromotionException(PromotionErrorCode.USER_VOUCHER_NOT_FOUND));
        Instant expiresAt = command.expiresAt() != null ? command.expiresAt()
                : Instant.now().plus(DEFAULT_RESERVATION_TTL);
        voucher.reserveSlot();
        uv.reserve(command.orderId(), expiresAt);

        voucherRepository.save(voucher);
        UserVoucher saved = userVoucherRepository.save(uv);
        eventPublisher.publish(uv.pullEvents());
        return mapper.toResult(saved);
    }

    public UserVoucherResult apply(VoucherCommands.ApplyVoucher command) {
        Voucher voucher = voucherRepository.lockByCode(command.voucherCode())
                .orElseThrow(() -> new PromotionException(PromotionErrorCode.VOUCHER_NOT_FOUND));
        PromotionCampaign campaign = campaignRepository.findById(voucher.getCampaignId())
                .orElseThrow(() -> new PromotionException(PromotionErrorCode.CAMPAIGN_NOT_FOUND));
        UserVoucher uv = userVoucherRepository.findByUserAndCode(command.userId(), command.voucherCode())
                .orElseThrow(() -> new PromotionException(PromotionErrorCode.USER_VOUCHER_NOT_FOUND));
        if (uv.getReservedOrderId() == null || !uv.getReservedOrderId().equals(command.orderId())) {
            throw new PromotionException(PromotionErrorCode.USER_VOUCHER_RESERVED_BY_OTHER);
        }
        Money applied = Money.of(command.appliedAmount(),
                command.currency() == null ? voucher.getDiscountAmount().currency() : command.currency());
        voucher.commitSlot();
        campaign.consumeBudget(applied);
        uv.apply(applied);

        voucherRepository.save(voucher);
        campaignRepository.save(campaign);
        UserVoucher saved = userVoucherRepository.save(uv);
        eventPublisher.publish(uv.pullEvents());
        return mapper.toResult(saved);
    }

    public UserVoucherResult release(VoucherCommands.ReleaseVoucher command) {
        Voucher voucher = voucherRepository.lockByCode(command.voucherCode())
                .orElseThrow(() -> new PromotionException(PromotionErrorCode.VOUCHER_NOT_FOUND));
        UserVoucher uv = userVoucherRepository.findByUserAndCode(command.userId(), command.voucherCode())
                .orElseThrow(() -> new PromotionException(PromotionErrorCode.USER_VOUCHER_NOT_FOUND));
        if (uv.getReservedOrderId() == null || !uv.getReservedOrderId().equals(command.orderId())) {
            throw new PromotionException(PromotionErrorCode.USER_VOUCHER_RESERVED_BY_OTHER);
        }
        voucher.releaseSlot();
        uv.release(command.reason());

        voucherRepository.save(voucher);
        UserVoucher saved = userVoucherRepository.save(uv);
        eventPublisher.publish(uv.pullEvents());
        return mapper.toResult(saved);
    }

    public int releaseExpiredReservations(Instant now, int batchSize) {
        // Kept for backwards compatibility; the auto-release scheduler now drives
        // expiry directly through VoucherAutoReleaseWorker so each release runs
        // in its own transaction and a single failure doesn't poison the batch.
        List<UserVoucher> expired = userVoucherRepository.findExpiredReservations(now, batchSize);
        int released = 0;
        for (UserVoucher uv : expired) {
            try {
                Voucher voucher = voucherRepository.lockByCode(uv.getVoucherCode())
                        .orElse(null);
                if (voucher != null) {
                    voucher.releaseSlot();
                    voucherRepository.save(voucher);
                }
                uv.release("expired");
                userVoucherRepository.save(uv);
                eventPublisher.publish(uv.pullEvents());
                released++;
            } catch (PromotionException ex) {
                log.warn("Skip release for {}: {}", uv.getUserVoucherId(), ex.getMessage());
            }
        }
        return released;
    }

    @Transactional(readOnly = true)
    public UserVoucherResult getMine(String userId, String voucherCode) {
        return mapper.toResult(userVoucherRepository.findByUserAndCode(userId, voucherCode)
                .orElseThrow(() -> new PromotionException(PromotionErrorCode.USER_VOUCHER_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public List<UserVoucherResult> listMine(String userId, int limit) {
        return userVoucherRepository.findByUser(userId, limit).stream().map(mapper::toResult).toList();
    }
}
