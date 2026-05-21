package com.aionn.config.security;

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
	private List<String> trustedProxies = new ArrayList<>();
	private List<RateLimitRule> rateLimits = new ArrayList<>();
	private Cors cors = new Cors();

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

	public void setTrustedProxies(List<String> trustedProxies) {
		if (trustedProxies == null) {
			this.trustedProxies = new ArrayList<>();
			return;
		}
		this.trustedProxies = trustedProxies.stream()
				.filter(s -> s != null && !s.isBlank())
				.map(String::trim)
				.toList();
	}

	@Getter
	@Setter
	public static class RateLimitRule {
		private String path;
		private String method;
		private int maxRequests = 5;
		private int windowSeconds = 60;
	}

	@Getter
	@Setter
	public static class Cors {
		private List<String> allowedOrigins = new ArrayList<>();
	}
}

