package com.shopizer.checkout.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables configuration properties binding for checkout-service.
 */
@Configuration
@EnableConfigurationProperties(ShopizerClientsProperties.class)
public class PropertiesConfig {}
