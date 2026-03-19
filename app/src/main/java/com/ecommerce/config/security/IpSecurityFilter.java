package com.ecommerce.config.security;

import com.ecommerce.sharedkernel.adapter.web.response.ApiResponse;
import com.ecommerce.sharedkernel.infrastructure.web.RequestAttributeKeys;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class IpSecurityFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;
	private final IpRateLimiter ipRateLimiter;
	private final SecurityIpProperties securityIpProperties;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		String clientIp = extractClientIp(request);
		request.setAttribute(RequestAttributeKeys.CLIENT_IP, clientIp);

		if (securityIpProperties.getBlacklist().contains(clientIp)) {
			log.warn("Blocked request from blacklisted IP: {}", clientIp);
			writeError(response, HttpStatus.FORBIDDEN, "Access denied: Your IP is blacklisted");
			return;
		}

		for (SecurityIpProperties.RateLimitRule rule : securityIpProperties.getRateLimits()) {
			if (matchesRule(request, rule)) {
				String key = String.format("%s:%s:%s", clientIp, rule.getMethod().toUpperCase(), rule.getPath());
				boolean allowed = ipRateLimiter.allow(key, rule.getMaxRequests(), rule.getWindowSeconds());
				if (!allowed) {
					log.warn("Rate limited IP {} on {} {}", clientIp, request.getMethod(), request.getRequestURI());
					writeError(response, HttpStatus.TOO_MANY_REQUESTS, "Too many requests from this IP");
					return;
				}
				break;
			}
		}
		filterChain.doFilter(request, response);
	}

	private boolean matchesRule(HttpServletRequest request, SecurityIpProperties.RateLimitRule rule) {
		if (rule.getPath() == null || rule.getMethod() == null) {
			return false;
		}
		return request.getRequestURI().equals(rule.getPath())
				&& request.getMethod().equalsIgnoreCase(rule.getMethod());
	}

	private String extractClientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		ApiResponse<Void> body = ApiResponse.error(String.valueOf(status.value()), message);
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}
}
