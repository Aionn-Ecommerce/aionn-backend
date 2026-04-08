package com.ecommerce.identity.application.port.in.consent;

import com.ecommerce.identity.application.dto.consent.command.AgreeTermsCommand;
import com.ecommerce.identity.application.dto.consent.result.ConsentResult;

public interface AgreeTermsInputPort {
    ConsentResult execute(AgreeTermsCommand command);
}
