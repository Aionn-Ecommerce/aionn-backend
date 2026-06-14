package com.aionn.ucp.infrastructure.gateway;

import com.aionn.ucp.application.port.out.PromotionQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionQueryGateway implements PromotionQueryPort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<DiscountInfo> validateCode(String code, String userId,
            long orderTotalMinor, String currency) {
        try {
            var vouchers = jdbcTemplate.query(
                    """
                            SELECT v.voucher_code, v.discount_amount, v.discount_currency,
                                   v.usage_limit, v.used_count, v.reserved_count,
                                   v.valid_from, v.valid_until,
                                   c.title, c.status AS campaign_status
                            FROM voucher v
                            JOIN promotion_campaign c ON c.campaign_id = v.campaign_id
                            WHERE LOWER(v.voucher_code) = LOWER(?)
                            """,
                    (rs, rowNum) -> new VoucherRow(
                            rs.getString("voucher_code"),
                            rs.getLong("discount_amount"),
                            rs.getString("discount_currency"),
                            rs.getInt("usage_limit"),
                            rs.getInt("used_count"),
                            rs.getInt("reserved_count"),
                            rs.getTimestamp("valid_from") != null ? rs.getTimestamp("valid_from").toInstant() : null,
                            rs.getTimestamp("valid_until") != null ? rs.getTimestamp("valid_until").toInstant() : null,
                            rs.getString("title"),
                            rs.getString("campaign_status")),
                    code);

            if (vouchers.isEmpty()) {
                return Optional.of(new DiscountInfo(code, null, 0, currency, "discount_code_invalid"));
            }

            VoucherRow row = vouchers.get(0);
            Instant now = Instant.now();

            // Check campaign status
            if (!"RUNNING".equalsIgnoreCase(row.campaignStatus())) {
                return Optional.of(new DiscountInfo(code, row.title(), 0, currency, "discount_code_expired"));
            }

            // Check expiration
            if (row.validUntil() != null && now.isAfter(row.validUntil())) {
                return Optional.of(new DiscountInfo(code, row.title(), 0, currency, "discount_code_expired"));
            }

            // Check valid from
            if (row.validFrom() != null && now.isBefore(row.validFrom())) {
                return Optional.of(new DiscountInfo(code, row.title(), 0, currency, "discount_code_invalid"));
            }

            // Check usage limit
            int remaining = row.usageLimit() - row.usedCount() - row.reservedCount();
            if (remaining <= 0) {
                return Optional.of(new DiscountInfo(code, row.title(), 0, currency, "discount_code_expired"));
            }

            // Valid — compute the discount amount (capped at order total)
            long discount = Math.min(row.discountAmount(), orderTotalMinor);
            return Optional.of(new DiscountInfo(
                    row.voucherCode(),
                    row.title(),
                    discount,
                    row.discountCurrency() != null ? row.discountCurrency() : currency,
                    null));

        } catch (Exception ex) {
            log.warn("Promotion query failed for code={}: {}", code, ex.getMessage());
            return Optional.of(new DiscountInfo(code, null, 0, currency, "discount_code_invalid"));
        }
    }

    private record VoucherRow(
            String voucherCode,
            long discountAmount,
            String discountCurrency,
            int usageLimit,
            int usedCount,
            int reservedCount,
            Instant validFrom,
            Instant validUntil,
            String title,
            String campaignStatus) {
    }
}
