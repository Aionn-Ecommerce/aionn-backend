package com.aionn.identity.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Domain model representing a security audit log entry.
 * Records security-related events and actions performed by users or agents.
 */
@Getter
@Builder
@AllArgsConstructor
public class SecurityAudit {

    private final String id;
    private final String userId;
    private final String eventType;
    private final String description;
    private final String ipAddress;
    private final String deviceId;
    private final LocalDateTime timestamp;

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

    /**
     * Checks if this audit log is related to an agent action.
     *
     * @return true if the description contains agent-related information
     */
    public boolean isAgentRelated() {
        return description != null && description.toLowerCase().contains("agent");
    }
}

