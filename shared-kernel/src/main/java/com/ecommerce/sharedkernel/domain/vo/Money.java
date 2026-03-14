package com.ecommerce.sharedkernel.domain.vo;

import com.ecommerce.sharedkernel.domain.model.ValueObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) implements ValueObject {

    public static final Money ZERO_VND = Money.of(BigDecimal.ZERO, "VND");
    public static final Money ZERO_USD = Money.of(BigDecimal.ZERO, "USD");
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public Money {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");

        int precision = currency.getDefaultFractionDigits() < 0 ? 2 : currency.getDefaultFractionDigits();
        amount = amount.setScale(precision, ROUNDING);
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    public static Money of(long amount, String currencyCode) {
        return of(BigDecimal.valueOf(amount), currencyCode);
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }

    public Money multiply(double factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    public Money calculateVat(double vatRate) {
        return multiply(BigDecimal.valueOf(vatRate));
    }

    public Money applyDiscount(int percent) {
        if (percent < 0 || percent > 100)
            throw new IllegalArgumentException("0-100%");
        BigDecimal multiplier = BigDecimal.valueOf(100 - percent)
                .divide(BigDecimal.valueOf(100), 2, ROUNDING);
        return multiply(multiplier);
    }

    public boolean isGreaterThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch!");
        }
    }
}