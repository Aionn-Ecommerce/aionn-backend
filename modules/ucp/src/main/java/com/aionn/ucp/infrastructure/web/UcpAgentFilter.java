package com.aionn.ucp.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class UcpAgentFilter extends OncePerRequestFilter {

    public static final String UCP_AGENT_HEADER = "UCP-Agent";
    public static final String PLATFORM_PROFILE_URL_ATTR = "ucp.platform.profile.url";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String ucpAgent = request.getHeader(UCP_AGENT_HEADER);
        if (ucpAgent != null && !ucpAgent.isBlank()) {
            request.setAttribute(PLATFORM_PROFILE_URL_ATTR, ucpAgent.trim());
            log.debug("UCP-Agent header present: {}", ucpAgent.trim());
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Only apply to UCP endpoints
        return !path.startsWith("/ucp/") && !path.startsWith("/.well-known/ucp");
    }
}
