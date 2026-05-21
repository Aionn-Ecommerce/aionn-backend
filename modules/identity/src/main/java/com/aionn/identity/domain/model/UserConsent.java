package com.aionn.identity.domain.model;

import com.aionn.identity.domain.valueobject.ConsentType;
import com.aionn.sharedkernel.util.IpAddressValidator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Immutable record of a single consent decision (grant or revoke). Each
 * decision is appended to {@code user_consents} so we keep the full history.
 */
@Getter
@Builder
@AllArgsConstructor
public class UserConsent {

    private final String id;
    private final String userId;
    private final ConsentType consentType;
    private final String version;
    private final boolean granted;
    private final LocalDateTime agreedAt;
    private final LocalDateTime revokedAt;
    private final String ipAddress;

    public boolean isActive() {
        return granted && revokedAt == null;
    }

    /**
     * @deprecated use {@link IpAddressValidator#isValid(String)} from
     *             shared-kernel. Kept for backward compatibility only.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public static boolean isValidIpAddress(String ipAddress) {
        return IpAddressValidator.isValid(ipAddress);
    }
}

