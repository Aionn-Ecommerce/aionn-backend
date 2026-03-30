package com.ecommerce.identity.application.port.in.consent;

import com.ecommerce.identity.application.dto.consent.AgreePrivacyCommand;
import com.ecommerce.identity.application.dto.consent.ConsentResult;

public interface AgreePrivacyInputPort {
    ConsentResult execute(AgreePrivacyCommand command);
}