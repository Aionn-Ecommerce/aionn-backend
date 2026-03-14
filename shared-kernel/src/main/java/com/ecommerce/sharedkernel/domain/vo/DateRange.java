package com.ecommerce.sharedkernel.domain.vo;

import com.ecommerce.sharedkernel.domain.model.ValueObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record DateRange(LocalDateTime startAt, LocalDateTime endAt) implements ValueObject {

    public DateRange {
        Objects.requireNonNull(startAt, "startAt must not be null");
        Objects.requireNonNull(endAt, "endAt must not be null");

        if (startAt.isAfter(endAt)) {
            throw new IllegalArgumentException(
                    "startAt must not be after endAt: %s > %s".formatted(startAt, endAt));
        }
    }

    public static DateRange of(LocalDateTime startAt, LocalDateTime endAt) {
        return new DateRange(startAt, endAt);
    }

    public static DateRange of(LocalDate startAt, LocalDate endAt) {
        return new DateRange(startAt.atStartOfDay(), endAt.atTime(23, 59, 59));
    }

    public static DateRange openEnd(LocalDateTime startAt) {
        return new DateRange(startAt, LocalDateTime.MAX);
    }

    public boolean contains(LocalDateTime point) {
        return !point.isBefore(startAt) && !point.isAfter(endAt);
    }

    public boolean contains(LocalDate date) {
        return contains(date.atStartOfDay());
    }

    public boolean isActiveNow() {
        return contains(LocalDateTime.now());
    }

    public boolean overlaps(DateRange other) {
        return !this.endAt.isBefore(other.startAt)
                && !other.endAt.isBefore(this.startAt);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endAt);
    }

    public boolean hasStarted() {
        return !LocalDateTime.now().isBefore(startAt);
    }

    @Override
    public String toString() {
        return "[%s → %s]".formatted(startAt, endAt);
    }
}