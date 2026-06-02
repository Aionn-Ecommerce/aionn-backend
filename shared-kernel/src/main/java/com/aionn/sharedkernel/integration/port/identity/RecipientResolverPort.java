package com.aionn.sharedkernel.integration.port.identity;

/**
 * Outbound port for resolving user contact information (email, phone, device
 * tokens).
 *
 * <p>
 * This port defines the contract for synchronous recipient resolution from the
 * Notification module.
 * It has two implementations:
 * </p>
 * <ul>
 * <li><strong>Monolith:</strong> In-memory adapter that directly queries
 * Identity module's UserPersistencePort</li>
 * <li><strong>Microservices:</strong> gRPC/HTTP client that calls Identity
 * service remotely</li>
 * </ul>
 * 
 * <p>
 * The Notification module uses this port to resolve userId → actual contact
 * information
 * (email address, phone number, or device token) without directly depending on
 * the Identity module's domain model.
 * </p>
 * 
 * <p>
 * <strong>Why synchronous?</strong> Recipient resolution must complete before
 * notification can be sent.
 * If resolution fails, the notification dispatch should fail immediately.
 * </p>
 */
public interface RecipientResolverPort {

    /**
     * Resolve a user ID to the actual recipient address for a given channel.
     * 
     * @param userId  the user ID
     * @param channel the notification channel (EMAIL, SMS, PUSH, IN_APP)
     * @return the recipient address (email, phone, device token, or in-app
     *         identifier)
     * @throws RecipientNotFoundException if user not found or channel not available
     */
    String resolve(String userId, String channel);
}
