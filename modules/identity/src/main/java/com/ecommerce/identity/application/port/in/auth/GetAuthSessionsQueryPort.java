package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.AuthSessionView;
import com.ecommerce.identity.application.dto.auth.GetAuthSessionsQuery;

import java.util.List;

public interface GetAuthSessionsQueryPort {

    List<AuthSessionView> execute(GetAuthSessionsQuery query);
}
