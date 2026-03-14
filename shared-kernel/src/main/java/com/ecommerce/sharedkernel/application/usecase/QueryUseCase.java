package com.ecommerce.sharedkernel.application.usecase;

import com.ecommerce.sharedkernel.application.query.Query;

public interface QueryUseCase<C extends Query, R> {
    R execute(C command);
}
