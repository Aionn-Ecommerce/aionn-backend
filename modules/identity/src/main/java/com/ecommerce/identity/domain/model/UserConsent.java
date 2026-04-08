package com.ecommerce.identity.domain.model;

import com.ecommerce.identity.domain.valueobject.ConsentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Domain model representing user consent.
 * Tracks user agreements to terms, privacy policies, and marketing
 * communications.
 */
@Getter
@Builder
@AllArgsConstructor
public class UserConsent {

    private final String id;
    private final String userId;
    private final ConsentType consentType;
    private final String version;
    private boolean granted;
    private final LocalDateTime agreedAt;
    private LocalDateTime revokedAt;
    private final String ipAddress;

    /**
     * Revokes this consent.
     */
    public void revoke() {
        this.granted = false;
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * Grants this consent.
     */
    public void grant() {
        this.granted = true;
        this.revokedAt = null;
    }

    /**
     * Checks if this consent is currently active (granted and not revoked).
     *
     * @return true if the consent is active, false otherwise
     */
    public boolean isActive() {
        return granted && revokedAt == null;
    }

    /**
     * Validates that the IP address is in a valid format.
     *
     * @param ipAddress the IP address to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return false;
        }
        // Basic IPv4 validation
        String ipv4Pattern = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
        // Basic IPv6 validation (simplified)
        String ipv6Pattern = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
        return ipAddress.matches(ipv4Pattern) || ipAddress.matches(ipv6Pattern);
    }
}
