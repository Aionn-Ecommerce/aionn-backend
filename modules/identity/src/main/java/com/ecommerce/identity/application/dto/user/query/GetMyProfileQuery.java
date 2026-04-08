package com.ecommerce.identity.application.dto.user.query;

import com.ecommerce.sharedkernel.application.query.Query;

public record GetMyProfileQuery(String userId) implements Query {
}
