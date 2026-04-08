package com.ecommerce.identity.application.port.in.consent;

import com.ecommerce.identity.application.dto.consent.command.UpdateMarketingConsentCommand;
import com.ecommerce.identity.application.dto.consent.result.ConsentResult;

public interface UpdateMarketingConsentInputPort {
    ConsentResult execute(UpdateMarketingConsentCommand command);
}
