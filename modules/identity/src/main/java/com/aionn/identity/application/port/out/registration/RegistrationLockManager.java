package com.aionn.identity.application.port.out.registration;

public interface RegistrationLockManager {

    String tryLock(String key, int timeoutSeconds);

    void unlock(String key, String lockToken);
}
