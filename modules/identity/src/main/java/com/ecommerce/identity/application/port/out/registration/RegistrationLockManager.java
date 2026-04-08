package com.ecommerce.identity.application.port.out.registration;

public interface RegistrationLockManager {

    boolean tryLock(String phoneNumber, int timeoutSeconds);

    void unlock(String phoneNumber);
}
