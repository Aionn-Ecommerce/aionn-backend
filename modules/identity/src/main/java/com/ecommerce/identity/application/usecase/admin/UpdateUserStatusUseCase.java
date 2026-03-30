package com.ecommerce.identity.application.usecase.admin;

import com.ecommerce.identity.application.dto.admin.UpdateUserStatusCommand;
import com.ecommerce.identity.application.dto.admin.UserStatusResult;
import com.ecommerce.identity.application.port.in.admin.UpdateUserStatusInputPort;
import com.ecommerce.identity.application.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserStatusUseCase implements UpdateUserStatusInputPort {

    private final AdminUserService adminUserService;

    @Override
    @Transactional
    public UserStatusResult execute(UpdateUserStatusCommand command) {
        var result = adminUserService.updateStatus(command.userId(), command.status());
        return new UserStatusResult(result);
    }
}
