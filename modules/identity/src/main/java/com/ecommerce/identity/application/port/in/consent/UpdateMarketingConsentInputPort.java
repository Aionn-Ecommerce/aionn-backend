package com.ecommerce.identity.application.port.in.consent;

import com.ecommerce.identity.application.dto.consent.UpdateMarketingConsentCommand;
import com.ecommerce.identity.application.dto.consent.ConsentResult;

public interface UpdateMarketingConsentInputPort {
    ConsentResult execute(UpdateMarketingConsentCommand command);
}