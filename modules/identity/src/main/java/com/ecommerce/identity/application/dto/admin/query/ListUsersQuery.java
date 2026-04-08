package com.ecommerce.identity.application.dto.admin.query;

import com.ecommerce.sharedkernel.application.query.Query;

public record ListUsersQuery(String status, String role, int page, int size) implements Query {
}

