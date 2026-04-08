package com.ecommerce.identity.application.usecase.admin;

import com.ecommerce.identity.application.dto.admin.command.RemoveUserRolesCommand;
import com.ecommerce.identity.application.dto.admin.result.UserRolesResult;
import com.ecommerce.identity.application.mapper.AdminResultMapper;
import com.ecommerce.identity.application.port.in.admin.RemoveUserRolesInputPort;
import com.ecommerce.identity.application.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveUserRolesUseCase implements RemoveUserRolesInputPort {

    private final AdminUserService adminUserService;
    private final AdminResultMapper adminResultMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRolesResult execute(RemoveUserRolesCommand command) {
        var result = adminUserService.removeRoles(command.userId(), command.roles());
        return adminResultMapper.toUserRolesResult(result);
    }
}
