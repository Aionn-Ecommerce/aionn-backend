package com.aionn.identity.domain.valueobject;

import com.aionn.sharedkernel.util.OtpGenerator;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value object representing a One-Time Password (OTP) for registration
 * verification.
 * 
 * <p>
 * This encapsulates OTP generation, expiry calculation, and resend cooldown
 * logic
 * to ensure consistency across registration flows.
 * </p>
 * 
 * <h3>OTP Rules:</h3>
 * <ul>
 * <li>OTP is a 6-digit numeric code generated using {@link OtpGenerator}</li>
 * <li>OTP has an expiry time calculated from creation time + configured expiry
 * seconds</li>
 * <li>OTP has a resend cooldown period to prevent abuse</li>
 * <li>OTP code is immutable once created</li>
 * </ul>
 * 
 * @see OtpGenerator
 */
public final class RegistrationOtp {

    private final String code;
    private final LocalDateTime resendAvailableAt;
    private final LocalDateTime expiredAt;

    private RegistrationOtp(String code, LocalDateTime resendAvailableAt, LocalDateTime expiredAt) {
        this.code = Objects.requireNonNull(code, "OTP code cannot be null");
        this.resendAvailableAt = Objects.requireNonNull(resendAvailableAt, "Resend available time cannot be null");
        this.expiredAt = Objects.requireNonNull(expiredAt, "Expiry time cannot be null");
    }

    /**
     * Generates a new OTP with the specified cooldown and expiry durations.
     * 
     * @param resendCooldownSeconds the number of seconds before OTP can be resent
     * @param expirySeconds         the number of seconds until OTP expires
     * @return a new RegistrationOtp instance
     * @throws IllegalArgumentException if cooldown or expiry seconds are negative
     */
    public static RegistrationOtp generate(int resendCooldownSeconds, int expirySeconds) {
        if (resendCooldownSeconds < 0) {
            throw new IllegalArgumentException("Resend cooldown seconds cannot be negative");
        }
        if (expirySeconds < 0) {
            throw new IllegalArgumentException("Expiry seconds cannot be negative");
        }

        String code = OtpGenerator.generate6DigitOtp();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resendAvailableAt = now.plusSeconds(resendCooldownSeconds);
        LocalDateTime expiredAt = now.plusSeconds(expirySeconds);

        return new RegistrationOtp(code, resendAvailableAt, expiredAt);
    }

    /**
     * Returns the 6-digit OTP code.
     * 
     * @return the OTP code
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the time when the OTP can be resent.
     * 
     * @return the resend available time
     */
    public LocalDateTime getResendAvailableAt() {
        return resendAvailableAt;
    }

    /**
     * Returns the time when the OTP expires.
     * 
     * @return the expiry time
     */
    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    /**
     * Checks if the OTP has expired.
     * 
     * @return true if the OTP has expired, false otherwise
     */
    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RegistrationOtp that = (RegistrationOtp) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(resendAvailableAt, that.resendAvailableAt) &&
                Objects.equals(expiredAt, that.expiredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, resendAvailableAt, expiredAt);
    }

    @Override
    public String toString() {
        return "RegistrationOtp{" +
                "code=***" + // Masked for security
                ", resendAvailableAt=" + resendAvailableAt +
                ", expiredAt=" + expiredAt +
                '}';
    }
}

