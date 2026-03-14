package com.ecommerce.sharedkernel.infrastructure.persistence;

import com.ecommerce.sharedkernel.application.port.UnitOfWork;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class SpringUnitOfWork implements UnitOfWork {

    private final TransactionTemplate transactionTemplate;

    public SpringUnitOfWork(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T executeInTransaction(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }
}
