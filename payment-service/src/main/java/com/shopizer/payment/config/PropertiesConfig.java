package com.shopizer.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables configuration properties binding for payment-service.
 */
@Configuration
@EnableConfigurationProperties({PayPalProperties.class})
public class PropertiesConfig {
}
