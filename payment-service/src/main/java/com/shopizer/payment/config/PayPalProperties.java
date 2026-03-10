package com.shopizer.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for PayPal integration.
 *
 * Bound from `payment.paypal.*` in application.yml.
 */
@ConfigurationProperties(prefix = "payment.paypal")
public record PayPalProperties(
    boolean enabled,
    String baseUrl,
    String clientId,
    String clientSecret
) {}
