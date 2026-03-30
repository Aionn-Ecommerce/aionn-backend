package com.ecommerce.identity.application.usecase.admin;

import com.ecommerce.identity.application.dto.admin.UpdateUserRolesCommand;
import com.ecommerce.identity.application.dto.admin.UserRolesResult;
import com.ecommerce.identity.application.port.in.admin.UpdateUserRolesInputPort;
import com.ecommerce.identity.application.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserRolesUseCase implements UpdateUserRolesInputPort {

    private final AdminUserService adminUserService;

    @Override
    @Transactional
    public UserRolesResult execute(UpdateUserRolesCommand command) {
        var result = adminUserService.updateRoles(command.userId(), command.roles());
        return new UserRolesResult(result);
    }
}
