package com.ecommerce.identity.adapter.rest.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientUserAgentResolver {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String UNKNOWN_USER_AGENT = "unknown";

    public String resolve(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT_HEADER);
        if (userAgent == null || userAgent.isBlank()) {
            return UNKNOWN_USER_AGENT;
        }
        return userAgent;
    }
}
