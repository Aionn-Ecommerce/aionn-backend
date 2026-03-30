package com.ecommerce.identity.application.usecase.admin;

import com.ecommerce.identity.application.dto.admin.ListUsersQuery;
import com.ecommerce.identity.application.dto.admin.UserListResult;
import com.ecommerce.identity.application.port.in.admin.ListUsersQueryPort;
import com.ecommerce.identity.application.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListUsersUseCase implements ListUsersQueryPort {

    private final AdminUserService adminUserService;

    @Override
    @Transactional(readOnly = true)
    public UserListResult execute(ListUsersQuery query) {
        return adminUserService.listUsers(query.status(), query.role(), query.page(), query.size());
    }
}
