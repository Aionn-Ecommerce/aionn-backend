package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.UnlockAccountCommand;
import com.ecommerce.identity.application.port.in.security.UnlockAccountInputPort;
import com.ecommerce.identity.application.service.AdminUserService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UnlockAccountUseCase implements UnlockAccountInputPort {

    private final AdminUserService adminUserService;

    @Override
    public void execute(UnlockAccountCommand command) {
        adminUserService.unlockAccount(command.userId());
    }
}
