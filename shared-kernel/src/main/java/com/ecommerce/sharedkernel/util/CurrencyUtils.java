package com.ecommerce.sharedkernel.util;

import com.ecommerce.sharedkernel.domain.vo.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyUtils {
    private CurrencyUtils() {
    }

    private static final Locale VN_LOCALE = Locale.of("vi", "VN");

    public static String formatVnd(Money money) {
        if (!"VND".equals(money.currency().getCurrencyCode())) {
            throw new IllegalArgumentException("Not a VND Money object");
        }
        NumberFormat nf = NumberFormat.getNumberInstance(VN_LOCALE);
        return nf.format(money.amount()) + " ₫";
    }

    public static String formatVndCompact(Money money) {
        BigDecimal amount = money.amount();
        if (amount.compareTo(new BigDecimal("1000000000")) >= 0) {
            return amount.divide(new BigDecimal("1000000000"), 1, RoundingMode.HALF_UP)
                    .stripTrailingZeros().toPlainString() + " tỷ";
        }
        if (amount.compareTo(new BigDecimal("1000000")) >= 0) {
            return amount.divide(new BigDecimal("1000000"), 1, RoundingMode.HALF_UP)
                    .stripTrailingZeros().toPlainString() + " tr";
        }
        return formatVnd(money);
    }
}