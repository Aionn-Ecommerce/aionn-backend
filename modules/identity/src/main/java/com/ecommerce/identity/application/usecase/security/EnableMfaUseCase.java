package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.command.EnableMfaCommand;
import com.ecommerce.identity.application.dto.security.result.MfaResult;
import com.ecommerce.identity.application.port.in.security.EnableMfaInputPort;
import com.ecommerce.identity.application.service.MfaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnableMfaUseCase implements EnableMfaInputPort {

    private final MfaService mfaService;

    @Override
    public MfaResult execute(EnableMfaCommand command) {
        var result = mfaService.enableMfa(command.userId(), command.password(), command.clientIp());
        return new MfaResult(result);
    }
}
