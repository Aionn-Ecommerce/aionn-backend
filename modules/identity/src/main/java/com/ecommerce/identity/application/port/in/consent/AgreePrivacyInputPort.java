package com.ecommerce.identity.application.port.in.consent;

import com.ecommerce.identity.application.dto.consent.command.AgreePrivacyCommand;
import com.ecommerce.identity.application.dto.consent.result.ConsentResult;

public interface AgreePrivacyInputPort {
    ConsentResult execute(AgreePrivacyCommand command);
}
