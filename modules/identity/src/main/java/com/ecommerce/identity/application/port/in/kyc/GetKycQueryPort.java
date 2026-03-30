package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.GetKycQuery;
import com.ecommerce.identity.application.dto.kyc.KycResult;

public interface GetKycQueryPort {
    KycResult execute(GetKycQuery query);
}