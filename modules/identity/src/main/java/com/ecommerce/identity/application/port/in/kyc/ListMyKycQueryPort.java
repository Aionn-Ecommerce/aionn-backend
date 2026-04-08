package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.result.KycResult;
import java.util.List;

public interface ListMyKycQueryPort {
    List<KycResult> execute(String userId);
}
