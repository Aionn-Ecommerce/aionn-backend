package com.ecommerce.identity.application.usecase.auth;

import com.ecommerce.identity.application.dto.auth.command.UnlinkSocialCommand;
import com.ecommerce.identity.application.port.in.auth.UnlinkSocialInputPort;
import com.ecommerce.identity.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnlinkSocialUseCase implements UnlinkSocialInputPort {

    private final AuthService authService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlinkSocial(String userId, String provider) {
        var command = new UnlinkSocialCommand(userId, provider);
        authService.unlinkSocial(command);
    }
}
