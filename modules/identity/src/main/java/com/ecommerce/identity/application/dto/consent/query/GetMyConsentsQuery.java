package com.ecommerce.identity.application.dto.consent.query;

import com.ecommerce.sharedkernel.application.query.Query;

public record GetMyConsentsQuery(String userId) implements Query {
}
