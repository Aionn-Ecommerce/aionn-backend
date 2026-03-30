package com.ecommerce.identity.application.usecase.admin;

import com.ecommerce.identity.application.dto.admin.GetUserQuery;
import com.ecommerce.identity.application.dto.admin.UserDetailResult;
import com.ecommerce.identity.application.port.in.admin.GetUserByIdQueryPort;
import com.ecommerce.identity.application.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserByIdUseCase implements GetUserByIdQueryPort {

    private final AdminUserService adminUserService;

    @Override
    @Transactional(readOnly = true)
    public UserDetailResult execute(GetUserQuery query) {
        return adminUserService.getUserById(query.userId());
    }
}
