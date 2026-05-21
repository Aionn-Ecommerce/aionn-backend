package com.aionn.identity.application.dto.admin.query;

import com.aionn.sharedkernel.application.query.Query;

public record ListUsersQuery(String status, String role, int page, int size) implements Query {
}


