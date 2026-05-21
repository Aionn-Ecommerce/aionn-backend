package com.aionn.identity.application.usecase.user;

import com.aionn.identity.application.dto.user.command.ChangePhoneCommand;
import com.aionn.identity.application.dto.user.view.UserActionOutcomeView;
import com.aionn.identity.application.port.in.user.ChangePhoneInputPort;
import com.aionn.identity.application.service.AccountManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePhoneUseCase implements ChangePhoneInputPort {

    private final AccountManagementService accountManagementService;

    @Override
    @Transactional
    public UserActionOutcomeView execute(ChangePhoneCommand command) {
        return accountManagementService.changePhone(command);
    }
}

