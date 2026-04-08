package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.query.GetKycQuery;
import com.ecommerce.identity.application.dto.kyc.result.KycResult;

public interface GetKycQueryPort {
    KycResult execute(GetKycQuery query);
}

