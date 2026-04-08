package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.view.AuthSessionView;
import com.ecommerce.identity.application.dto.auth.query.GetAuthSessionsQuery;

import java.util.List;

public interface GetAuthSessionsQueryPort {

    List<AuthSessionView> execute(GetAuthSessionsQuery query);
}
