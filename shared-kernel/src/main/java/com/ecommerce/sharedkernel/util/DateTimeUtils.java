package com.ecommerce.sharedkernel.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static final ZoneId UTC = ZoneId.of("UTC");
    public static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private static final DateTimeFormatter VN_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter VN_DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    public static Instant nowUtc() {
        return Instant.now();
    }

    public static LocalDateTime nowLocal(ZoneId zone) {
        return LocalDateTime.now(zone);
    }

    public static LocalDateTime nowVn() {
        return LocalDateTime.now(VN);
    }

    public static LocalDate todayVn() {
        return LocalDate.now(VN);
    }

    public static boolean isPast(Instant instant) {
        return instant.isBefore(Instant.now());
    }

    public static boolean isFuture(Instant instant) {
        return instant.isAfter(Instant.now());
    }

    public static boolean isExpired(Instant expiresAt) {
        return isPast(expiresAt);
    }

    public static boolean isBetween(Instant target, Instant start, Instant end) {
        return !target.isBefore(start) && !target.isAfter(end);
    }

    public static Instant startOfDay(LocalDate date, ZoneId zone) {
        return date.atStartOfDay(zone).toInstant();
    }

    public static Instant endOfDay(LocalDate date, ZoneId zone) {
        return date.atTime(LocalTime.MAX).atZone(zone).toInstant();
    }

    public static Instant startOfDayVn(LocalDate date) {
        return startOfDay(date, VN);
    }

    public static Instant endOfDayVn(LocalDate date) {
        return endOfDay(date, VN);
    }

    public static long daysBetween(Instant from, Instant to) {
        return ChronoUnit.DAYS.between(from, to);
    }

    public static long hoursBetween(Instant from, Instant to) {
        return ChronoUnit.HOURS.between(from, to);
    }

    public static long minutesBetween(Instant from, Instant to) {
        return ChronoUnit.MINUTES.between(from, to);
    }

    public static String formatVn(LocalDate date) {
        return date.format(VN_DATE);
    }

    public static String formatVn(LocalDateTime dateTime) {
        return dateTime.format(VN_DATETIME);
    }

    public static String formatVn(Instant instant) {
        return formatVn(instant.atZone(VN).toLocalDateTime());
    }

    public static String formatIso(LocalDate date) {
        return date.format(ISO_DATE);
    }

    public static LocalDate toLocalDate(Instant instant, ZoneId zone) {
        return instant.atZone(zone).toLocalDate();
    }

    public static LocalDate toLocalDateVn(Instant instant) {
        return toLocalDate(instant, VN);
    }

    public static Instant toInstant(LocalDateTime localDateTime, ZoneId zone) {
        return localDateTime.atZone(zone).toInstant();
    }

    public static Instant toInstantVn(LocalDateTime localDateTime) {
        return toInstant(localDateTime, VN);
    }
}
