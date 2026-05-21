package com.aionn.inventory.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's scheduler so
 * {@link com.aionn.inventory.infrastructure.scheduling.ReservationAutoReleaseScheduler}
 * runs. Lives inside the inventory module so the bootstrap app does not need
 * to know about it.
 */
@Configuration
@EnableScheduling
public class InventorySchedulingConfig {
}

