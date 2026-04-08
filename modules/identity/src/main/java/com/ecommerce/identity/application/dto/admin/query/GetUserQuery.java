package com.ecommerce.identity.application.dto.admin.query;

import com.ecommerce.sharedkernel.application.query.Query;

public record GetUserQuery(String userId) implements Query {
}
