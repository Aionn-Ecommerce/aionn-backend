package com.ecommerce.identity.application.port.in.consent;

import com.ecommerce.identity.application.dto.consent.AgreeTermsCommand;
import com.ecommerce.identity.application.dto.consent.ConsentResult;

public interface AgreeTermsInputPort {
    ConsentResult execute(AgreeTermsCommand command);
}