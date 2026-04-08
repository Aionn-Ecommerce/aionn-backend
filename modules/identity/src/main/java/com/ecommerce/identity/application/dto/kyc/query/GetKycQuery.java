package com.ecommerce.identity.application.dto.kyc.query;

import com.ecommerce.sharedkernel.application.query.Query;

public record GetKycQuery(String userId, String kycId) implements Query {
}
