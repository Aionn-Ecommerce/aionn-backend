package com.aionn.identity.application.usecase.user;

import com.aionn.identity.application.dto.user.command.ChangeEmailCommand;
import com.aionn.identity.application.dto.user.view.UserActionOutcomeView;
import com.aionn.identity.application.port.in.user.ChangeEmailInputPort;
import com.aionn.identity.application.service.AccountManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeEmailUseCase implements ChangeEmailInputPort {

    private final AccountManagementService accountManagementService;

    @Override
    @Transactional
    public UserActionOutcomeView execute(ChangeEmailCommand command) {
        return accountManagementService.changeEmail(command);
    }
}

