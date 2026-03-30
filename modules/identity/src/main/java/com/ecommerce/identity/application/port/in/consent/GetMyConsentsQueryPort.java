package com.ecommerce.identity.application.port.in.consent;

import com.ecommerce.identity.application.dto.consent.ConsentResult;
import java.util.List;

public interface GetMyConsentsQueryPort {
    List<ConsentResult> execute(String userId);
}