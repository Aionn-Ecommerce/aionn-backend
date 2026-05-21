package com.aionn.identity.application.port.out.registration;

/**
 * Distributed lock used to serialize concurrent registration completions for
 * the same phone number.
 *
 * <p>
 * The lock returns a token on acquire which must be passed back to
 * {@link #unlock(String, String)}. Adapters should reject unlock when the
 * token does not match the stored value to avoid the classic
 * "delete-someone-else's-lock" bug.
 */
public interface RegistrationLockManager {

    /**
     * Try to acquire the lock for {@code phoneNumber}.
     *
     * @return non-empty token when the lock was acquired; empty string when
     *         another worker already holds it.
     */
    String tryLock(String phoneNumber, int timeoutSeconds);

    void unlock(String phoneNumber, String token);
}

