package com.ecommerce.identity.application.dto.kyc;

import com.ecommerce.sharedkernel.application.query.Query;

public record GetKycQuery(String userId, String kycId) implements Query {
}