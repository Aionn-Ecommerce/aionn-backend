package com.ecommerce.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiModuleConfig {

	@Bean
	public GroupedOpenApi identityModuleApi() {
		return moduleApi("Identity", "com.ecommerce.identity.adapter.rest.controller");
	}

	@Bean
	public GroupedOpenApi catalogModuleApi() {
		return moduleApi("Catalog", "com.ecommerce.catalog.adapter.rest.controller");
	}

	private GroupedOpenApi moduleApi(String groupName, String controllerPackage) {
		return GroupedOpenApi.builder()
				.group(groupName)
				.packagesToScan(controllerPackage)
				.build();
	}
}
