package com.aionn.identity.adapter.rest.config;

import com.aionn.identity.adapter.rest.support.ClientUserAgentArgumentResolver;
import com.aionn.sharedkernel.adapter.web.support.ClientIpArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RestAdapterConfig implements WebMvcConfigurer {

	private final ClientIpArgumentResolver clientIpArgumentResolver;
	private final ClientUserAgentArgumentResolver clientUserAgentArgumentResolver;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(clientIpArgumentResolver);
		resolvers.add(clientUserAgentArgumentResolver);
	}
}

