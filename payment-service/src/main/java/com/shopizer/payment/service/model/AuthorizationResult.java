package com.shopizer.payment.service.model;

/**
 * Result model from provider authorization attempt.
 *
 * providerAuthorizationId/providerOrderId may be null depending on provider behavior.
 */
public record AuthorizationResult(
    String providerOrderId,
    String providerAuthorizationId,
    String rawResponse
) {}
