package com.ecommerce.sharedkernel.application.port;

import java.util.function.Supplier;

public interface UnitOfWork {

    <T> T executeInTransaction(Supplier<T> work);

    default void executeInTransaction(Runnable work) {
        executeInTransaction(() -> {
            work.run();
            return null;
        });
    }
}