package com.ecommerce.identity.application.dto.admin;

import com.ecommerce.sharedkernel.application.query.Query;

public record GetUserQuery(String userId) implements Query {
}