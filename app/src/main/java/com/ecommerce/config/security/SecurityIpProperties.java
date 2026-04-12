package com.ecommerce.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "security.ip")
@Getter
@Setter
public class SecurityIpProperties {

	private Set<String> blacklist = new HashSet<>();
	private List<RateLimitRule> rateLimits = new ArrayList<>();

	public void setBlacklist(Set<String> blacklist) {
		if (blacklist == null) {
			this.blacklist = new HashSet<>();
			return;
		}
		this.blacklist = blacklist.stream()
				.filter(s -> s != null && !s.isBlank())
				.map(String::trim)
				.collect(Collectors.toSet());
	}

	@Getter
	@Setter
	public static class RateLimitRule {
		private String path;
		private String method;
		private int maxRequests;
		private int windowSeconds;
	}
}
