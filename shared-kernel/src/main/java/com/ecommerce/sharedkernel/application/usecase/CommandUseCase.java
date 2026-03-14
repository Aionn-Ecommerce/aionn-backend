package com.ecommerce.sharedkernel.application.usecase;

import com.ecommerce.sharedkernel.application.command.Command;

public interface CommandUseCase<C extends Command, R> {
    R execute(C command);
}
