package com.ecommerce.identity.application.dto.auth;

import com.ecommerce.sharedkernel.application.query.Query;

public record GetAuthSessionsQuery(String userId) implements Query {
}
