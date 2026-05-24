package com.aionn.identity.adapter.rest.support;

import com.aionn.identity.application.port.out.auth.AuthClientPolicy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthClientTypeResolver {

    private final AuthClientPolicy authClientPolicy;

    public String resolve(HttpServletRequest request) {
        return request.getHeader(authClientPolicy.getClientTypeHeader());
    }
}
