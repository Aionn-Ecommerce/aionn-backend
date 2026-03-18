package com.ecommerce.sharedkernel.testing;

import com.ecommerce.sharedkernel.domain.id.BaseId;
import com.ecommerce.sharedkernel.domain.model.AggregateRoot;
import com.ecommerce.sharedkernel.domain.model.DomainEvent;
import com.ecommerce.sharedkernel.domain.vo.Money;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.math.BigDecimal;
import java.util.List;

public final class DomainAssertions {

    private DomainAssertions() {
    }

    public static <ID extends BaseId> AggregateAssert<ID> assertThat(
            AggregateRoot<ID> aggregate) {
        return new AggregateAssert<>(aggregate);
    }

    public static MoneyAssert assertThat(Money money) {
        return new MoneyAssert(money);
    }

    // ── AggregateAssert ───────────────────────────────────────────────────────

    public static class AggregateAssert<ID extends BaseId>
            extends AbstractAssert<AggregateAssert<ID>, AggregateRoot<ID>> {

        private AggregateAssert(AggregateRoot<ID> aggregate) {
            super(aggregate, AggregateAssert.class);
        }

        public AggregateAssert<ID> hasPublishedEvent(Class<? extends DomainEvent> eventType) {
            isNotNull();
            List<DomainEvent> events = actual.peekDomainEvents();
            boolean found = events.stream().anyMatch(eventType::isInstance);
            if (!found) {
                failWithMessage("Expected aggregate to have published event <%s> but it didn't. Published: <%s>",
                        eventType.getSimpleName(),
                        events.stream().map(e -> e.getClass().getSimpleName()).toList());
            }
            return this;
        }

        public AggregateAssert<ID> hasNotPublishedEvent(Class<? extends DomainEvent> eventType) {
            isNotNull();
            boolean found = actual.peekDomainEvents().stream().anyMatch(eventType::isInstance);
            if (found) {
                failWithMessage("Expected aggregate NOT to have published event <%s>, but it did.",
                        eventType.getSimpleName());
            }
            return this;
        }

        public AggregateAssert<ID> hasExactlyNEvents(int expectedCount) {
            isNotNull();
            int actual = this.actual.peekDomainEvents().size();
            if (actual != expectedCount) {
                failWithMessage("Expected exactly <%d> domain event(s), but got <%d>.",
                        expectedCount, actual);
            }
            return this;
        }

        public AggregateAssert<ID> hasNoPublishedEvents() {
            return hasExactlyNEvents(0);
        }

        public <E extends DomainEvent> E extractEvent(Class<E> eventType) {
            isNotNull();
            return actual.peekDomainEvents().stream()
                    .filter(eventType::isInstance)
                    .map(eventType::cast)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(
                            "No event of type " + eventType.getSimpleName() + " found"));
        }
    }

    // ── MoneyAssert ───────────────────────────────────────────────────────────

    public static class MoneyAssert extends AbstractAssert<MoneyAssert, Money> {

        private MoneyAssert(Money money) {
            super(money, MoneyAssert.class);
        }

        public MoneyAssert hasAmount(String expectedAmount) {
            isNotNull();
            BigDecimal expected = new BigDecimal(expectedAmount);
            if (actual.amount().compareTo(expected) != 0) {
                failWithMessage("Expected Money amount to be <%s> but was <%s>",
                        expectedAmount, actual.amount().toPlainString());
            }
            return this;
        }

        public MoneyAssert hasCurrency(String currencyCode) {
            isNotNull();
            Assertions.assertThat(actual.currency().getCurrencyCode())
                    .as("Money currency")
                    .isEqualTo(currencyCode);
            return this;
        }

        public MoneyAssert isZero() {
            isNotNull();
            if (actual.amount().compareTo(BigDecimal.ZERO) != 0) {
                failWithMessage("Expected Money to be zero but was <%s>", actual);
            }
            return this;
        }

        public MoneyAssert isGreaterThan(Money other) {
            isNotNull();
            if (!actual.isGreaterThan(other)) {
                failWithMessage("Expected <%s> to be greater than <%s>", actual, other);
            }
            return this;
        }

        public MoneyAssert isLessThan(Money other) {
            isNotNull();
            if (!actual.currency().equals(other.currency())) {
                failWithMessage("Expected currencies to match but was <%s> and <%s>",
                        actual.currency(), other.currency());
            }
            if (actual.amount().compareTo(other.amount()) >= 0) {
                failWithMessage("Expected <%s> to be less than <%s>", actual, other);
            }
            return this;
        }
    }
}
